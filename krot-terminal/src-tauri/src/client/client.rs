use crate::client::api::model::{AuthenticationCredentials, RequestMetadata};
use crate::client::api::request_model::{AuthenticationRequest, DisconnectRequest};
use crate::client::api::response_model::AuthenticationResponse;
use crate::client::confidential::{Credentials, Session};
use crate::client::error::ClientError;
use crate::client::types::converters::{b16uuid_to_string, b64_to_bytes, bytes_to_b64};
use crate::crypto::cipher::{ChaCha20Poly1305Cipher, Decryptor, Encryptor};
use crate::crypto::ephemeral::EphemeralEngine;
use crate::security::keystore::{ApplicationKeystore, CredentialsKeystore, Keystore, SessionKeystore};
use bytes::Bytes;
use rand::rngs::SysRng;
use rand_core::TryRng;
use reqwest::header::{HeaderMap, HeaderName, HeaderValue};
use reqwest::{Client, RequestBuilder, Response};
use std::io::Write;
use std::sync::Arc;
use x25519_dalek::PublicKey;

#[derive(Debug, PartialEq, Eq)]
pub enum RequestType {
    Get,
    Post,
    Patch,
}

pub struct KrotClient {
    pub http: Client,
    keystore: Arc<ApplicationKeystore>,
    cipher: ChaCha20Poly1305Cipher,
    ephemeral_engine: EphemeralEngine,
    addr: [u8; 4],
    port: u16,
    secured: bool,
}

impl KrotClient {
    pub fn new(keystore: Arc<ApplicationKeystore>) -> Self {
        let mut client = KrotClient {
            http: Client::new(),
            keystore,
            cipher: ChaCha20Poly1305Cipher,
            ephemeral_engine: EphemeralEngine,
            addr: [0u8; 4],
            port: 0,
            secured: false,
        };
        let _ = client.refresh_credentials();
        client
    }

    pub async fn hello(client: &Client, addr: &[u8; 4], port: &u16, secured: bool) -> Result<(), ClientError> {
        let mut path_buf = [0u8; 256];
        path_buf[..6].copy_from_slice(b"/hello");
        Self::req_get_unauthenticated(client, addr, port, &path_buf, secured, &mut [0u8; 24]).await?;
        Ok(())
    }

    pub async fn pubkey(&self, out: &mut [u8; 32]) -> Result<(), ClientError> {
        let mut path_buf = [0u8; 256];
        path_buf[..7].copy_from_slice(b"/pubkey");
        let mut res_buf = [0u8; 44];
        Self::req_get_unauthenticated(
            &self.http,
            &self.addr,
            &self.port,
            &path_buf,
            self.secured,
            &mut res_buf,
        )
            .await?;
        b64_to_bytes(&res_buf, out)?;
        Ok(())
    }

    #[inline(always)]
    fn inject_b64_header<const S: usize>(
        headers: &mut HeaderMap,
        name: &'static str,
        data: &[u8],
    ) -> Result<(), ClientError> {
        let mut b64_buf = [0u8; S];
        let len =
            bytes_to_b64(data, &mut b64_buf).map_err(|_| ClientError::HeaderCompositionFailed)?;
        let b64_str = std::str::from_utf8(&b64_buf[..len]).map_err(|_| ClientError::HeaderCompositionFailed)?;
        headers.insert(
            HeaderName::from_static(name),
            HeaderValue::from_str(b64_str).map_err(|_| ClientError::HeaderCompositionFailed)?,
        );
        Ok(())
    }

    #[inline(always)]
    async fn req_get_unauthenticated(
        client: &Client,
        addr: &[u8; 4],
        port: &u16,
        path: &[u8; 256],
        secured: bool,
        out: &mut [u8],
    ) -> Result<usize, ClientError> {
        let mut url_buf = [0u8; 278];
        Self::url(addr, port, path, secured, &mut url_buf)?;
        let url =
            std::str::from_utf8(&url_buf[0..url_buf.iter().position(|&b| b == 0).unwrap_or(278)])
                .map_err(|_| ClientError::UrlError)?;
        let response = client
            .get(url)
            .send()
            .await
            .map_err(|_| ClientError::NetworkError)?;
        if !response.status().is_success() {
            return Err(match response.status() {
                reqwest::StatusCode::BAD_REQUEST => ClientError::BadRequest,
                reqwest::StatusCode::UNAUTHORIZED => ClientError::Unauthorized,
                reqwest::StatusCode::FORBIDDEN => ClientError::Forbidden,
                reqwest::StatusCode::NOT_FOUND => ClientError::NotFound,
                _ => ClientError::ServiceUnavailable,
            });
        }
        let res_buf = response
            .bytes()
            .await
            .map_err(|_| ClientError::NetworkError)?;
        if res_buf.len() > out.len() {
            return Err(ClientError::BufferTooSmall);
        }
        out[..res_buf.len()].copy_from_slice(&res_buf);
        Ok(res_buf.len())
    }

    #[inline(always)]
    fn url(
        addr: &[u8; 4],
        port: &u16,
        path: &[u8; 256],
        secured: bool,
        out: &mut [u8],
    ) -> Result<(), ClientError> {
        let mut cursor = out;
        let scheme = if secured { "https" } else { "http" };
        let path_len = path.iter().position(|&b| b == 0).unwrap_or(256);
        let path_str = std::str::from_utf8(&path[..path_len]).map_err(|_| ClientError::UrlError)?;
        write!(
            &mut cursor,
            "{}://{}.{}.{}.{}:{}{}",
            scheme, addr[0], addr[1], addr[2], addr[3], port, path_str
        ).map_err(|_| ClientError::UrlError)?;
        Ok(())
    }

    pub async fn req<
        T: serde::Serialize,
        E: serde::de::DeserializeOwned,
        const ST: usize,
        const SE: usize,
    >(
        &self,
        method: &RequestType,
        path: &[u8; 256],
        data: &T,
        encrypt: bool,
        decrypt: bool,
        out: &mut E,
    ) -> Result<(), ClientError> {
        match self.chk_session() {
            Ok(session) => session,
            Err(ClientError::SessionAboutToExpire) => self.refresh_session().await?,
            Err(ClientError::SessionExpired) => {
                self.keystore
                    .session_keystore
                    .drop(SessionKeystore::IDX)
                    .map_err(|_| ClientError::SessionExpired)?;
                return Err(ClientError::SessionExpired);
            }
            Err(e) => return Err(e),
        };
        let session = self.get_session()?;
        let mut url_buf = [0u8; 278];
        Self::url(&self.addr, &self.port, path, self.secured, &mut url_buf)?;
        let url =
            std::str::from_utf8(&url_buf[0..url_buf.iter().position(|&b| b == 0).unwrap_or(278)])
                .map_err(|_| ClientError::UrlError)?;
        let mut req = match method {
            RequestType::Get => self.http.get(url),
            RequestType::Post => self.http.post(url),
            RequestType::Patch => self.http.patch(url),
        };
        let mut body_buf = [0u8; ST];
        let body_len = self.generate_body(data, &mut body_buf)?;
        let mut nonce_buf = [0u8; 12];
        let mut tag_buf = [0u8; 16];
        if !method.eq(&RequestType::Get) {
            req = self.set_body(
                req,
                &mut body_buf[..body_len],
                encrypt,
                &session.encryption_key,
                &mut nonce_buf,
                &mut tag_buf,
            )?;
        }
        if !method.eq(&RequestType::Get) {
            req =
                self.set_authentication_headers(req, &session.reference_key, &nonce_buf, &tag_buf)?;
        }
        let response = req.send().await.map_err(|_| ClientError::NetworkError)?;
        let mut res_nonce_buf = [0u8; 12];
        let mut res_tag_buf = [0u8; 16];
        if decrypt {
            let res_nonce = response.headers().get("x-response-nonce").ok_or(ClientError::DecryptionFailed)?;
            let res_tag = response.headers().get("x-response-tag").ok_or(ClientError::DecryptionFailed)?;
            b64_to_bytes(&res_nonce.as_bytes(), &mut res_nonce_buf)?;
            b64_to_bytes(&res_tag.as_bytes(), &mut res_tag_buf)?;
        }
        let status_code = response.status().as_u16();
        if status_code > 399 {
            return Err(match status_code {
                400 => ClientError::BadRequest,
                401 => ClientError::Unauthorized,
                403 => ClientError::Forbidden,
                404 => ClientError::NotFound,
                409 => ClientError::Conflict,
                503 => ClientError::ServiceUnavailable,
                _ => ClientError::InternalServerError,
            });
        }
        self
            .process_response::<E, SE>(
                response,
                decrypt,
                &session.encryption_key,
                &res_nonce_buf,
                &res_tag_buf,
                out,
            )
            .await?;
        Ok(())
    }

    pub async fn req_get<E: serde::de::DeserializeOwned, const SE: usize>(
        &self,
        path: &[u8; 256],
        decrypt: bool,
        out: &mut E,
    ) -> Result<(), ClientError> {
        self.req::<[u8; 0], E, 0, SE>(&RequestType::Get, path, &mut [0u8; 0], false, decrypt, out)
            .await
    }

    pub async fn req_post<
        T: serde::Serialize,
        E: serde::de::DeserializeOwned,
        const ST: usize,
        const SE: usize,
    >(
        &self,
        path: &[u8; 256],
        data: &T,
        encrypt: bool,
        decrypt: bool,
        out: &mut E,
    ) -> Result<(), ClientError> {
        self.req::<T, E, ST, SE>(&RequestType::Post, path, data, encrypt, decrypt, out)
            .await
    }

    pub async fn req_patch<
        T: serde::Serialize,
        E: serde::de::DeserializeOwned,
        const ST: usize,
        const SE: usize,
    >(
        &self,
        path: &[u8; 256],
        data: &T,
        encrypt: bool,
        decrypt: bool,
        out: &mut E,
    ) -> Result<(), ClientError> {
        self.req::<T, E, ST, SE>(&RequestType::Patch, path, data, encrypt, decrypt, out)
            .await
    }

    pub async fn authenticate(
        &self,
        out: &mut AuthenticationCredentials,
    ) -> Result<(), ClientError> {
        let credentials = self.get_credentials()?;
        let mut url_buf = [0u8; 278];
        let mut path_buf = [0u8; 256];
        path_buf[0..19].copy_from_slice(b"/api/auth/handshake");
        Self::url(&self.addr, &self.port, &path_buf, self.secured, &mut url_buf)?;
        let url =
            std::str::from_utf8(&url_buf[0..url_buf.iter().position(|&b| b == 0).unwrap_or(278)])
                .map_err(|_| ClientError::UrlError)?;
        let mut req = self.http.post(url);
        let mut nonce_buf = [0u8; 12];
        let mut tag_buf = [0u8; 16];
        let mut key_buf = [0u8; 32];
        let mut client_pubkey_buf = [0u8; 32];
        self.generate_handshake_key(&mut key_buf, &mut client_pubkey_buf)
            .await?;
        let mut req_body = AuthenticationRequest::default();
        req_body.identifier = credentials.name;
        req_body.password = credentials.pwd;
        let mut body_buf = [0u8; 2048];
        let body_len = self.generate_body(&req_body, &mut body_buf)?;
        req = self.set_body(
            req,
            &mut body_buf[..body_len],
            true,
            &mut key_buf,
            &mut nonce_buf,
            &mut tag_buf,
        )?;
        req = self.set_handshake_headers(req, &client_pubkey_buf, &nonce_buf, &tag_buf)?;
        let response = req.send().await.map_err(|_| ClientError::NetworkError)?;
        let mut response_body = AuthenticationResponse::default();
        let res_nonce = response.headers().get("x-response-nonce").ok_or(ClientError::DecryptionFailed)?;
        let res_tag = response.headers().get("x-response-tag").ok_or(ClientError::DecryptionFailed)?;
        let mut res_nonce_buf = [0u8; 12];
        let mut res_tag_buf = [0u8; 16];
        b64_to_bytes(&res_nonce.as_bytes(), &mut res_nonce_buf)?;
        b64_to_bytes(&res_tag.as_bytes(), &mut res_tag_buf)?;
        let status_code = response.status().as_u16();
        if status_code > 399 {
            return Err(match status_code {
                400 => ClientError::BadRequest,
                403 => ClientError::Forbidden,
                404 => ClientError::NotFound,
                503 => ClientError::ServiceUnavailable,
                _ => ClientError::InternalServerError,
            });
        }
        self.process_response::<AuthenticationResponse, 2048>(
            response,
            true,
            &key_buf,
            &res_nonce_buf,
            &res_tag_buf,
            &mut response_body,
        )
            .await?;
        *out = response_body.data;
        Ok(())
    }

    pub async fn disconnect(&self) -> Result<(), ClientError> {
        self.chk_session()?;
        let session = self.get_session()?;
        let mut path_buf = [0u8; 256];
        path_buf[..20].copy_from_slice(b"/api/auth/disconnect");
        let req_body = DisconnectRequest {
            metadata: RequestMetadata {
                session_id: session.id,
                timestamp: std::time::SystemTime::now()
                    .duration_since(std::time::UNIX_EPOCH)
                    .map_err(|_| ClientError::TimestampError)?
                    .as_millis() as i64,
            }
        };
        self.req_post::<DisconnectRequest, [u8; 0], 512, 0>(
            &path_buf,
            &req_body,
            true,
            false,
            &mut [0u8; 0],
        )
            .await?;
        self.keystore.session_keystore.drop(SessionKeystore::IDX).unwrap_or(());
        Ok(())
    }

    pub fn chk_session(&self) -> Result<(), ClientError> {
        let session = self.get_session()?;
        let timestamp = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .map_err(|_| ClientError::TimestampError)?
            .as_millis() as i64;
        let expiration_diff = session.expiration - timestamp;
        if expiration_diff > 0 && expiration_diff < 1000 * 60 * 5 {
            return Err(ClientError::SessionAboutToExpire);
        }
        if expiration_diff <= 0 {
            self.keystore.session_keystore.drop(SessionKeystore::IDX).unwrap_or(());
            return Err(ClientError::SessionExpired);
        }
        Ok(())
    }

    pub async fn refresh_session(&self) -> Result<(), ClientError> {
        let mut authentication_credentials = AuthenticationCredentials::default();
        self.authenticate(&mut authentication_credentials).await?;
        let _ = self.keystore.session_keystore.drop(SessionKeystore::IDX);
        self.keystore
            .session_keystore
            .store(
                SessionKeystore::IDX,
                &Session {
                    id: authentication_credentials.session_id,
                    reference_key: authentication_credentials.session_ref,
                    encryption_key: authentication_credentials.encryption_key,
                    expiration: authentication_credentials.expiration,
                },
            )
            .map_err(|_| ClientError::SessionNotFound)?;
        Ok(())
    }

    pub fn refresh_credentials(&mut self) -> Result<(), ClientError> {
        let credentials = self.get_credentials()?;
        self.addr = credentials.addr;
        self.port = credentials.port;
        self.secured = credentials.secured;
        Ok(())
    }

    fn get_session(&self) -> Result<Session, ClientError> {
        let mut session = Session {
            id: [0; 16],
            reference_key: [0; 16],
            encryption_key: [0; 32],
            expiration: 0,
        };
        self.keystore
            .session_keystore
            .read(SessionKeystore::IDX, &mut session)
            .map_err(|_| ClientError::SessionNotFound)?;
        Ok(session)
    }

    fn get_credentials(&self) -> Result<Credentials, ClientError> {
        let mut credentials = Credentials {
            name: [0; 128],
            pwd: [0; 128],
            addr: [0; 4],
            port: 0,
            secured: false,
        };
        self.keystore
            .credentials_keystore
            .read(CredentialsKeystore::IDX, &mut credentials)
            .map_err(|_| ClientError::CredentialsNotFound)?;
        Ok(credentials)
    }

    #[inline(always)]
    fn set_authentication_headers(
        &self,
        req: RequestBuilder,
        session_reference: &[u8; 16],
        nonce: &[u8; 12],
        tag: &[u8; 16],
    ) -> Result<RequestBuilder, ClientError> {
        let mut headers = HeaderMap::new();
        let mut uuid_buf = [0u8; 36];
        b16uuid_to_string(session_reference, &mut uuid_buf);
        headers.insert(
            HeaderName::from_static("x-session-reference"),
            HeaderValue::from_str(
                std::str::from_utf8(&uuid_buf).map_err(|_| ClientError::HeaderCompositionFailed)?,
            )
                .map_err(|_| ClientError::HeaderCompositionFailed)?,
        );
        Self::inject_b64_header::<16>(&mut headers, "x-request-nonce", nonce)?;
        Self::inject_b64_header::<24>(&mut headers, "x-request-tag", tag)?;
        Ok(req.headers(headers))
    }

    #[inline(always)]
    fn set_handshake_headers(
        &self,
        req: RequestBuilder,
        client_pubkey: &[u8; 32],
        nonce: &[u8; 12],
        tag: &[u8; 16],
    ) -> Result<RequestBuilder, ClientError> {
        let mut headers = HeaderMap::new();
        Self::inject_b64_header::<44>(&mut headers, "x-key", client_pubkey)?;
        Self::inject_b64_header::<16>(&mut headers, "x-request-nonce", nonce)?;
        Self::inject_b64_header::<24>(&mut headers, "x-request-tag", tag)?;
        Ok(req.headers(headers))
    }

    #[inline(always)]
    fn set_body(
        &self,
        req: RequestBuilder,
        body: &mut [u8],
        encrypt: bool,
        key: &[u8],
        nonce_out: &mut [u8; 12],
        tag_out: &mut [u8; 16],
    ) -> Result<RequestBuilder, ClientError> {
        SysRng
            .try_fill_bytes(nonce_out)
            .map_err(|_| ClientError::NonceGenerationFailed)?;
        if encrypt && body.len() > 0 {
            self.cipher
                .encrypt(body, key, nonce_out, tag_out)
                .map_err(|_| ClientError::EncryptionFailed)?;
        }
        Ok(req.body(Bytes::copy_from_slice(body)))
    }

    #[inline(always)]
    async fn generate_handshake_key(
        &self,
        key_buf: &mut [u8; 32],
        client_pubkey_buf: &mut [u8; 32],
    ) -> Result<(), ClientError> {
        let mut server_pubkey_buf = [0u8; 32];
        self.pubkey(&mut server_pubkey_buf).await?;
        let (client_pubkey, secret) = self.ephemeral_engine.generate_x25519_keypair();
        client_pubkey_buf[..32].copy_from_slice(client_pubkey.as_ref());
        let shared_secret = self
            .ephemeral_engine
            .generate_shared_key(&PublicKey::from(server_pubkey_buf), secret);
        key_buf[..32].copy_from_slice(
            &self
                .ephemeral_engine
                .derive_hkdf_key(&shared_secret)
                .map_err(|_| ClientError::EncryptionFailed)?[..],
        );
        Ok(())
    }

    #[inline(always)]
    fn generate_body<T: serde::Serialize>(
        &self,
        data: &T,
        out: &mut [u8],
    ) -> Result<usize, ClientError> {
        let out_buf_len = out.len();
        let mut cur = &mut out[..];
        serde_json::to_writer(&mut cur, data).map_err(|e| {
            println!("{}", e);
            ClientError::BodyCompositionFailed
        })?;
        Ok(out_buf_len - cur.len())
    }

    #[inline(always)]
    async fn process_response<T: serde::de::DeserializeOwned, const S: usize>(
        &self,
        response: Response,
        decrypt: bool,
        key: &[u8; 32],
        nonce: &[u8; 12],
        tag: &[u8; 16],
        out: &mut T,
    ) -> Result<(), ClientError> {
        if S > 0 {
            let response_buf = response
                .bytes()
                .await
                .map_err(|_| ClientError::NetworkError)?;
            let mut out_buf = [0u8; S];
            if response_buf.len() > out_buf.len() {
                return Err(ClientError::BufferTooSmall);
            }
            out_buf[..response_buf.len()].copy_from_slice(&response_buf);
            if decrypt && response_buf.len() > 0 {
                self.cipher
                    .decrypt(
                        &mut out_buf[..response_buf.len()],
                        &key[..],
                        &tag[..],
                        &nonce[..],
                    )
                    .map_err(|_| ClientError::DecryptionFailed)?;
            }
            *out = serde_json::from_slice::<T>(&out_buf[..response_buf.len()])
                .map_err(|e| {
                    println!("{}", e);
                    ClientError::BodyParseFailed
                })?;
        }
        Ok(())
    }
}
