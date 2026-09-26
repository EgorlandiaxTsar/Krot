use crate::client::api::gateway::ApiGateway;
use crate::client::api::model::auth::{AuthenticationCredentials, DisconnectRequest};
use crate::client::api::model::common::{BlankResponse, RequestMetadata};
use crate::client::error::ClientError;
use crate::client::secret::credentials::{Credentials, CredentialsHolder};
use crate::client::secret::session::{Session, SessionHolder};
use crate::client::utils;
use crate::client::ws::gateway::WsGateway;
use crate::security::keystore::ApplicationKeystore;
use std::sync::Arc;

const fn estimate_b64_len(len: usize) -> usize {
    4 * (len.div_ceil(3))
}

pub const KEY_BUFFER_LEN: usize = 32;
pub const TAG_BUFFER_LEN: usize = 16;
pub const NONCE_BUFFER_LEN: usize = 12;
pub const SESSION_ID_BUFFER_LEN: usize = 16;
pub const SESSION_REF_BUFFER_LEN: usize = 16;
pub const KEY_BUFFER_B64_LEN: usize = estimate_b64_len(KEY_BUFFER_LEN);
pub const TAG_BUFFER_B64_LEN: usize = estimate_b64_len(TAG_BUFFER_LEN);
pub const NONCE_BUFFER_B64_LEN: usize = estimate_b64_len(NONCE_BUFFER_LEN);
pub const SESSION_REF_BUFFER_B64_LEN: usize = estimate_b64_len(SESSION_REF_BUFFER_LEN);
pub const PATH_BUFFER_LEN: usize = 256;

pub type KeyBuffer = [u8; KEY_BUFFER_LEN];
pub type TagBuffer = [u8; TAG_BUFFER_LEN];
pub type NonceBuffer = [u8; NONCE_BUFFER_LEN];
pub type SessionIdBuffer = [u8; SESSION_ID_BUFFER_LEN];
pub type SessionRefBuffer = [u8; SESSION_REF_BUFFER_LEN];
pub type PathBuffer = [u8; PATH_BUFFER_LEN];
pub type IpBuffer = [u8; 4];


pub struct KrotClient {
    session: Arc<SessionHolder>,
    credentials: Arc<CredentialsHolder>,
    api: ApiGateway,
    ws: WsGateway,
    refresh_lock: Arc<tokio::sync::Mutex<()>>,
}

impl KrotClient {
    pub fn new(keystore: Arc<ApplicationKeystore>) -> Self {
        let session_holder_arc = Arc::new(SessionHolder::new(keystore.clone()));
        let credentials_holder_arc = Arc::new(CredentialsHolder::new(keystore.clone()));
        let client = Self {
            session: session_holder_arc.clone(),
            credentials: credentials_holder_arc.clone(),
            api: ApiGateway::new(session_holder_arc.clone(), credentials_holder_arc.clone()),
            ws: WsGateway::new(session_holder_arc.clone(), credentials_holder_arc.clone()),
            refresh_lock: Arc::from(tokio::sync::Mutex::new(())),
        };
        client.init_keystore();
        client
    }

    pub fn authenticated(&self) -> bool { self.session.current().is_ok() }

    pub fn credentials(&self) -> Result<Credentials, ClientError> { self.credentials.current() }

    pub fn session(&self) -> Result<Session, ClientError> { self.session.current() }

    pub async fn api(&self) -> Result<&ApiGateway, ClientError> {
        self.check_session().await?;
        Ok(&self.api)
    }

    pub async fn ws(&self) -> Result<&WsGateway, ClientError> {
        self.check_session().await?;
        Ok(&self.ws)
    }

    pub async fn authenticate(&self) -> Result<(), ClientError> {
        let mut credentials = AuthenticationCredentials::default();
        self.api.authenticate(&mut credentials).await?;
        self.session.store(Session {
            id: credentials.session_id,
            reference_key: credentials.session_ref,
            encryption_key: credentials.encryption_key,
            expiration: credentials.expiration,
        })
    }

    pub async fn disconnect(&self) -> Result<(), ClientError> {
        if let Ok(session) = self.session.current() {
            match self.api.disconnect(
                &DisconnectRequest { metadata: RequestMetadata::new(session.id) },
                &mut BlankResponse::default(),
            ).await {
                Ok(()) | Err(ClientError::SessionExpired) | Err(ClientError::SessionNotFound) => {}
                Err(e) => return Err(e),
            }
        }
        self.session.clear();
        Ok(())
    }

    pub async fn update_server_address(&self, addr: [u8; 4], port: u16, secured: bool) -> Result<(), ClientError> {
        let previous = self.credentials.current()?;
        let mut candidate = previous;
        candidate.addr = addr;
        candidate.port = port;
        candidate.secured = secured;
        self.credentials.store(candidate)?;

        if let Err(e) = self.api.hello().await {
            let _ = self.credentials.store(previous);
            return Err(e);
        }
        self.disconnect().await
    }

    pub async fn update_credentials(&self, username: &str, password: &str) -> Result<(), ClientError> {
        let mut candidate = self.credentials.current()?;
        candidate.name = utils::new_buffer(username.as_bytes());
        candidate.pwd = utils::new_buffer(password.as_bytes());
        self.credentials.store(candidate)?;

        if self.session.current().is_ok() {
            self.disconnect().await?;
            self.authenticate().await?;
        }
        Ok(())
    }

    // No-session functions forwarding
    pub async fn hello(&self) -> Result<(), ClientError> { self.api.hello().await }

    pub async fn pubkey(&self, out: &mut KeyBuffer) -> Result<(), ClientError> { self.api.pubkey(out).await }

    fn init_keystore(&self) {
        if self.credentials.current().is_err() {
            self.session.clear();
            let _ = self.credentials.store(Credentials::default());
        }
    }

    async fn check_session(&self) -> Result<(), ClientError> {
        match self.session.check() {
            Ok(()) => Ok(()),
            Err(ClientError::SessionAboutToExpire) => {
                let _guard = self.refresh_lock.lock().await;
                match self.session.check() {
                    Ok(()) => return Ok(()),
                    Err(ClientError::SessionExpired) => {
                        self.session.clear();
                        return Err(ClientError::SessionExpired);
                    }
                    _ => {}
                }

                self.authenticate().await?;
                Ok(())
            }
            Err(ClientError::SessionExpired) => {
                self.session.clear();
                Err(ClientError::SessionExpired)
            }
            Err(e) => Err(e)
        }
    }
}
