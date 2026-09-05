use crate::client::api::model::common::RequestMetadata;
use crate::client::api::model::request::WsAuthenticationRequest;
use crate::client::client::{KeyBuffer, NonceBuffer, TagBuffer, NONCE_BUFFER_LEN, PATH_BUFFER_LEN, SESSION_REF_BUFFER_LEN, TAG_BUFFER_LEN};
use crate::client::error::ClientError;
use crate::client::secret::credentials::{Credentials, CredentialsHolder};
use crate::client::secret::session::SessionHolder;
use crate::client::types::converters;
use crate::client::utils::{new_url, new_ws_url};
use crate::client::ws::gateway::WS_EVENT_CHANNEL_CAPACITY;
use crate::client::ws::state::WsChannelState;
use crate::client::ws::types::{Socket, SocketReader, SocketWriter};
use crate::crypto::cipher::{ChaCha20Poly1305Cipher, Decryptor, Encryptor};
use bytes::Bytes;
use chacha20poly1305::aead::common::getrandom::SysRng;
use futures_util::{SinkExt, StreamExt};
use rand_core::TryRng;
use std::io::Write;
use std::sync::atomic::Ordering;
use std::sync::Arc;
use tokio::sync::broadcast;
use tokio_tungstenite::connect_async;
use tokio_tungstenite::tungstenite::Message;

pub const WS_PATH_BUFFER_LEN: usize = PATH_BUFFER_LEN + 20;
pub const FRAME_HEADER_LEN: usize = TAG_BUFFER_LEN + NONCE_BUFFER_LEN;
pub const HANDSHAKE_HEADER_LEN: usize = SESSION_REF_BUFFER_LEN + FRAME_HEADER_LEN;
pub const HANDSHAKE_CONTENT_MAX_LEN: usize = 2048;

pub trait WsChannel: Clone + Send + Sync + Sized + 'static {
    fn path(&self) -> &'static str;
    fn session(&self) -> &Arc<SessionHolder>;
    fn credentials(&self) -> &Arc<CredentialsHolder>;
    fn cipher(&self) -> &ChaCha20Poly1305Cipher;
    fn state(&self) -> &WsChannelState;

    fn alive(&self) -> bool {
        self.state().alive.load(Ordering::Acquire)
    }

    fn subscribe(&self) -> broadcast::Receiver<Bytes> {
        self.state().events.subscribe()
    }

    fn disconnect(&self) {
        if let Some(handle) = self.state().task.lock().unwrap().take() { handle.abort() }
        self.state().alive.store(false, Ordering::Release)
    }

    async fn send<T: serde::Serialize, const S: usize>(&self, body: &mut T) -> Result<(), ClientError> {
        let session = self.session().current()?;

        let mut tag_buf: TagBuffer = [0u8; TAG_BUFFER_LEN];
        let mut nonce_buf: NonceBuffer = [0u8; NONCE_BUFFER_LEN];

        let mut body_buf = [0u8; S];
        let body_buf_len = converters::body_to_bytes(body, &mut body_buf)?;
        self.encrypt_body(&mut body_buf[..body_buf_len], &session.encryption_key, &mut tag_buf, &mut nonce_buf)?;

        let mut frame = Vec::with_capacity(FRAME_HEADER_LEN + body_buf.len());
        frame.extend_from_slice(&tag_buf);
        frame.extend_from_slice(&nonce_buf);
        frame.extend_from_slice(&body_buf);

        let mut guard = self.state().writer.lock().await;
        let writer = guard.as_mut().ok_or(ClientError::WsNotConnected)?;
        writer.send(Message::Binary(frame.into())).await.map_err(|_| ClientError::WsSendFailed)
    }

    async fn connect(&self) -> Result<(), ClientError> {
        if self.alive() { return Ok(()); }
        let credentials = self.credentials().current()?;

        let socket = self.build_socket(&credentials).await?;
        let (mut writer, mut reader) = socket.split();
        self.authenticate(&mut writer, &mut reader).await?;

        *self.state().writer.lock().await = Some(writer);
        self.state().alive.store(true, Ordering::Release);

        let worker = self.clone();
        let handle = tokio::spawn(worker.listen(reader).await);
        *self.state().task.lock().unwrap() = Some(handle);

        Ok(())
    }

    async fn build_socket(&self, credentials: &Credentials) -> Result<Socket, ClientError> {
        let mut url_buf = [0u8; WS_PATH_BUFFER_LEN];
        new_ws_url(&credentials.addr, &credentials.port, &new_url(self.path().as_bytes()), credentials.secured, &mut url_buf)?;
        let url_buf_len = url_buf.iter().position(|&byte| byte == 0).unwrap_or(WS_PATH_BUFFER_LEN);
        let url = str::from_utf8(&url_buf[..url_buf_len]).map_err(|_| ClientError::UrlError)?;

        let (socket, _) = connect_async(url).await.map_err(|_| ClientError::WsConnectionFailed)?;

        Ok(socket)
    }

    async fn authenticate(&self, writer: &mut SocketWriter, reader: &mut SocketReader) -> Result<(), ClientError> {
        let session = self.session().current()?;

        let mut tag_buf: TagBuffer = [0u8; TAG_BUFFER_LEN];
        let mut nonce_buf: NonceBuffer = [0u8; NONCE_BUFFER_LEN];

        let body = WsAuthenticationRequest { metadata: RequestMetadata::new(session.id) };
        let mut body_buf = [0u8; HANDSHAKE_CONTENT_MAX_LEN];
        let body_buf_len = converters::body_to_bytes(&body, &mut body_buf)?;
        self.encrypt_body(&mut body_buf[..body_buf_len], &session.encryption_key, &mut tag_buf, &mut nonce_buf)?;

        let mut frame = [0u8; HANDSHAKE_HEADER_LEN + HANDSHAKE_CONTENT_MAX_LEN];
        let mut cursor = &mut frame[..];
        cursor.write_all(&session.reference_key).map_err(|_| ClientError::BodyCompositionFailed)?;
        cursor.write_all(&tag_buf).map_err(|_| ClientError::BodyCompositionFailed)?;
        cursor.write_all(&nonce_buf).map_err(|_| ClientError::BodyCompositionFailed)?;
        cursor.write_all(&body_buf[..body_buf_len]).map_err(|_| ClientError::BodyCompositionFailed)?;
        let written_len = (HANDSHAKE_HEADER_LEN + HANDSHAKE_CONTENT_MAX_LEN) - cursor.len();
        let frame = &frame[..written_len];

        writer.send(Message::Binary(Bytes::copy_from_slice(frame))).await.map(|_| ClientError::WsHandshakeFailed).map_err(|_| ClientError::WsHandshakeFailed)?;
        match reader.next().await {
            Some(Ok(Message::Binary(status))) => if !status.is_empty() && status[0] == 1 { Ok(()) } else { Err(ClientError::WsHandshakeFailed) }, // If authentication is successful, a single byte equal to 1 is returned (no encryption or any complex data structures)
            _ => Err(ClientError::WsHandshakeFailed)
        }
    }

    async fn listen(self, mut reader: SocketReader) -> impl std::future::Future<Output=()> + Send {
        async move {
            loop {
                match reader.next().await {
                    Some(Ok(Message::Binary(bytes))) => self.handle_incoming(bytes),
                    Some(Ok(Message::Ping(_) | Message::Pong(_))) => continue,
                    Some(Ok(Message::Close(_))) | None => break,
                    Some(Ok(_)) => continue,
                    Some(Err(_)) => break,
                }
            }
            self.state().alive.store(false, Ordering::Release);
            *self.state().writer.lock().await = None;
        }
    }

    fn handle_incoming(&self, frame: Bytes) {
        let session = match self.session().current() {
            Ok(s) => s,
            Err(_) => return,
        };
        let mut buf = frame.to_vec();
        if let Ok(start) = self.decrypt_frame(&mut buf, &session.encryption_key) {
            let _ = self.state().events.send(Bytes::copy_from_slice(&buf[start..]));
        }
    }

    fn encrypt_body(
        &self,
        body: &mut [u8],
        key: &[u8],
        tag: &mut TagBuffer,
        nonce: &mut NonceBuffer,
    ) -> Result<(), ClientError> {
        SysRng.try_fill_bytes(nonce).map_err(|_| ClientError::NonceGenerationFailed)?;
        self.cipher().encrypt(body, key, nonce, tag).map_err(|_| ClientError::EncryptionFailed)?;
        Ok(())
    }

    fn decrypt_frame(&self, frame: &mut Vec<u8>, key: &KeyBuffer) -> Result<usize, ClientError> {
        if frame.len() < FRAME_HEADER_LEN { return Err(ClientError::WsProtocolViolation); }
        let tag_buf: TagBuffer = frame[..TAG_BUFFER_LEN].try_into().map_err(|_| ClientError::WsProtocolViolation)?;
        let nonce_buf: NonceBuffer = frame[TAG_BUFFER_LEN..FRAME_HEADER_LEN].try_into().map_err(|_| ClientError::WsProtocolViolation)?;
        self.cipher().decrypt(&mut frame[FRAME_HEADER_LEN..], key, &tag_buf, &nonce_buf).map_err(|_| ClientError::DecryptionFailed)?;
        Ok(FRAME_HEADER_LEN)
    }
}

#[derive(Clone)]
pub struct EventsChannel {
    session: Arc<SessionHolder>,
    credentials: Arc<CredentialsHolder>,
    cipher: ChaCha20Poly1305Cipher,
    state: WsChannelState,
}

impl EventsChannel {
    pub fn new(session: Arc<SessionHolder>, credentials: Arc<CredentialsHolder>) -> Self {
        Self {
            session,
            credentials,
            cipher: ChaCha20Poly1305Cipher,
            state: WsChannelState::new(WS_EVENT_CHANNEL_CAPACITY),
        }
    }
}

impl WsChannel for EventsChannel {
    fn path(&self) -> &'static str {
        "/wsapi/events"
    }

    fn session(&self) -> &Arc<SessionHolder> {
        &self.session
    }

    fn credentials(&self) -> &Arc<CredentialsHolder> {
        &self.credentials
    }

    fn cipher(&self) -> &ChaCha20Poly1305Cipher {
        &self.cipher
    }

    fn state(&self) -> &WsChannelState {
        &self.state
    }
}
