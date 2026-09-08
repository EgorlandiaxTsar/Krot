use crate::client::api::model::auth::{ApiAuthenticationRequest, AuthenticationCredentials, AuthenticationResponse, DisconnectRequest};
use crate::client::api::model::common::RequestMetadata;
use crate::client::client::{KeyBuffer, NonceBuffer, PathBuffer, SessionRefBuffer, TagBuffer, KEY_BUFFER_B64_LEN, KEY_BUFFER_LEN, NONCE_BUFFER_B64_LEN, NONCE_BUFFER_LEN, PATH_BUFFER_LEN, TAG_BUFFER_B64_LEN, TAG_BUFFER_LEN};
use crate::client::error::ClientError;
use crate::client::secret::credentials::CredentialsHolder;
use crate::client::secret::session::SessionHolder;
use crate::client::types::converters;
use crate::client::utils::{new_http_url, new_url};
use crate::crypto::cipher::{ChaCha20Poly1305Cipher, Decryptor, Encryptor};
use crate::crypto::ephemeral::EphemeralEngine;
use bytes::{Bytes, BytesMut};
use rand::rngs::SysRng;
use rand_core::TryRng;
use reqwest::header::{HeaderMap, HeaderName, HeaderValue};
use std::sync::Arc;

pub const HTTP_PATH_BUFFER_LEN: usize = PATH_BUFFER_LEN + 22;
pub const HANDSHAKE_CONTENT_MAX_LEN: usize = 2048;

pub const HANDSHAKE_PUBKEY_HEADER_NAME: &str = "x-key";
pub const SESSION_REF_HEADER_NAME: &str = "x-session-reference";
pub const REQUEST_TAG_HEADER_NAME: &str = "x-request-tag";
pub const REQUEST_NONCE_HEADER_NAME: &str = "x-request-nonce";
pub const RESPONSE_TAG_HEADER_NAME: &str = "x-response-tag";
pub const RESPONSE_NONCE_HEADER_NAME: &str = "x-response-nonce";

#[derive(Debug, PartialEq, Eq)]
pub enum RequestType {
    Get,
    Post,
    Put,
    Patch,
}

pub struct ApiGateway {
    http: reqwest::Client,
    cipher: ChaCha20Poly1305Cipher,
    ephemeral_engine: EphemeralEngine,
    session: Arc<SessionHolder>,
    credentials: Arc<CredentialsHolder>,
}

impl ApiGateway {
    pub fn new(session: Arc<SessionHolder>, credentials: Arc<CredentialsHolder>) -> Self {
        Self {
            http: reqwest::Client::new(),
            cipher: ChaCha20Poly1305Cipher,
            ephemeral_engine: EphemeralEngine,
            session,
            credentials,
        }
    }

    pub async fn hello(&self) -> Result<(), ClientError> {
        self.unauthenticated_request(&new_url(b"/hello"), &mut [0u8; 24]).await?;
        Ok(())
    }


    pub async fn pubkey(&self, out: &mut KeyBuffer) -> Result<(), ClientError> {
        let mut res_buf = [0u8; 44];
        self.unauthenticated_request(&new_url(b"/pubkey"), &mut res_buf).await?;
        converters::b64_to_bytes(&res_buf, out)?;
        Ok(())
    }

    pub async fn authenticate(&self, out: &mut AuthenticationCredentials) -> Result<(), ClientError> {
        let credentials = self.credentials.current()?;

        let mut url_buf = [0u8; HTTP_PATH_BUFFER_LEN];
        new_http_url(&credentials.addr, &credentials.port, &new_url(b"/api/auth/handshake"), credentials.secured, &mut url_buf)?;
        let url_buf_len = url_buf.iter().position(|&byte| byte == 0).unwrap_or(HTTP_PATH_BUFFER_LEN);
        let url = str::from_utf8(&url_buf[..url_buf_len]).map_err(|_| ClientError::UrlError)?;

        let mut key_buf: KeyBuffer = [0u8; KEY_BUFFER_LEN];
        let mut client_pubkey_buf = [0u8; KEY_BUFFER_LEN];
        self.generate_handshake_key(&mut key_buf, &mut client_pubkey_buf).await?;

        let body = ApiAuthenticationRequest::new(credentials.name, credentials.pwd);
        let mut body_buf = [0u8; HANDSHAKE_CONTENT_MAX_LEN];
        let body_buf_len = converters::body_to_bytes(&body, &mut body_buf)?;

        let mut req = self.http.post(url);
        let mut tag_buf: TagBuffer = [0u8; TAG_BUFFER_LEN];
        let mut nonce_buf: NonceBuffer = [0u8; NONCE_BUFFER_LEN];
        req = self.set_body(
            req,
            &mut body_buf[..body_buf_len],
            Some(&key_buf),
            &mut tag_buf,
            &mut nonce_buf,
        )?;
        req = self.set_handshake_headers(req, &client_pubkey_buf, &tag_buf, &nonce_buf)?;

        let res = req.send().await.map_err(|_| ClientError::NetworkError)?;
        let mut body = AuthenticationResponse::default();
        self.handle_response::<AuthenticationResponse, HANDSHAKE_CONTENT_MAX_LEN>(res, Some(&key_buf), &mut body).await?;
        *out = body.data;

        Ok(())
    }

    pub async fn disconnect(&self) -> Result<(), ClientError> {
        let session = self.session.current()?;
        self.post::<DisconnectRequest, [u8; 0], 512, 0>(
            &new_url(b"/api/auth/disconnect"),
            Some(&DisconnectRequest {
                metadata: RequestMetadata::new(session.id)
            }),
            true,
            true,
            &mut [0u8; 0],
        ).await?;
        self.session.clear();
        Ok(())
    }

    pub async fn get<E: serde::de::DeserializeOwned + Default, const SE: usize>(
        &self,
        path: &PathBuffer,
        decrypt: bool,
        out: &mut E,
    ) -> Result<(), ClientError> {
        self.req::<[u8; 0], E, 0, SE>(
            RequestType::Get,
            path,
            None,
            false,
            decrypt,
            out,
        ).await
    }

    pub async fn post<T: serde::Serialize + Default, E: serde::de::DeserializeOwned + Default, const ST: usize, const SE: usize>(
        &self,
        path: &PathBuffer,
        body: Option<&T>,
        encrypt: bool,
        decrypt: bool,
        out: &mut E,
    ) -> Result<(), ClientError> {
        self.req::<T, E, ST, SE>(
            RequestType::Post,
            path,
            body,
            encrypt,
            decrypt,
            out,
        ).await
    }

    pub async fn patch<T: serde::Serialize + Default, E: serde::de::DeserializeOwned + Default, const ST: usize, const SE: usize>(
        &self,
        path: &PathBuffer,
        body: Option<&T>,
        encrypt: bool,
        decrypt: bool,
        out: &mut E,
    ) -> Result<(), ClientError> {
        self.req::<T, E, ST, SE>(
            RequestType::Patch,
            path,
            body,
            encrypt,
            decrypt,
            out,
        ).await
    }

    pub async fn put<T: serde::Serialize + Default, E: serde::de::DeserializeOwned + Default, const ST: usize, const SE: usize>(
        &self,
        path: &PathBuffer,
        body: Option<&T>,
        encrypt: bool,
        decrypt: bool,
        out: &mut E,
    ) -> Result<(), ClientError> {
        self.req::<T, E, ST, SE>(
            RequestType::Put,
            path,
            body,
            encrypt,
            decrypt,
            out,
        ).await
    }

    async fn req<
        T: serde::Serialize + Default,
        E: serde::de::DeserializeOwned + Default,
        const ST: usize,
        const SE: usize,
    >(
        &self,
        method: RequestType,
        path: &PathBuffer,
        body: Option<&T>,
        encrypt: bool,
        decrypt: bool,
        out: &mut E,
    ) -> Result<(), ClientError> {
        self.session.check()?;
        let session = self.session.current()?;
        let credentials = self.credentials.current()?;

        let mut url_buf = [0u8; HTTP_PATH_BUFFER_LEN];
        new_http_url(&credentials.addr, &credentials.port, &new_url(path), credentials.secured, &mut url_buf)?;
        let url_buf_len = url_buf.iter().position(|&byte| byte == 0).unwrap_or(HTTP_PATH_BUFFER_LEN);
        let url = str::from_utf8(&url_buf[..url_buf_len]).map_err(|_| ClientError::UrlError)?;

        let mut req = match method {
            RequestType::Get => self.http.get(url),
            RequestType::Post => self.http.post(url),
            RequestType::Put => self.http.put(url),
            RequestType::Patch => self.http.patch(url),
        };
        let mut tag_buf: TagBuffer = [0u8; TAG_BUFFER_LEN];
        let mut nonce_buf: NonceBuffer = [0u8; NONCE_BUFFER_LEN];
        let has_body = body.is_some();
        if let Some(actual_body) = body {
            let mut body_buf = [0u8; ST];
            let body_buf_len = converters::body_to_bytes::<T>(actual_body, &mut body_buf)?;
            req = self.set_body(
                req,
                &mut body_buf[..body_buf_len],
                if encrypt { Some(&session.encryption_key) } else { None },
                &mut tag_buf,
                &mut nonce_buf,
            )?;
        }
        req = self.set_authentication_headers(
            req,
            &session.reference_key,
            if has_body { Some(&tag_buf) } else { None },
            if has_body { Some(&nonce_buf) } else { None },
        )?;

        let res = req.send().await.map_err(|_| ClientError::NetworkError)?;
        self.handle_response::<E, SE>(res, if decrypt { Some(&session.encryption_key) } else { None }, out).await?;

        Ok(())
    }

    fn unwrap_http_errors(&self, status_code: u16) -> Result<(), ClientError> {
        match status_code {
            400 => Err(ClientError::BadRequest),
            401 => Err(ClientError::Unauthorized),
            403 => Err(ClientError::Forbidden),
            404 => Err(ClientError::NotFound),
            409 => Err(ClientError::Conflict),
            500 => Err(ClientError::InternalServerError),
            503 => Err(ClientError::ServiceUnavailable),
            _ => Ok(())
        }
    }

    fn set_body(
        &self,
        req: reqwest::RequestBuilder,
        body: &mut [u8],
        key: Option<&[u8]>,
        tag_out: &mut TagBuffer,
        nonce_out: &mut NonceBuffer,
    ) -> Result<reqwest::RequestBuilder, ClientError> {
        SysRng.try_fill_bytes(nonce_out).map_err(|_| ClientError::NonceGenerationFailed)?;
        if let Some(actual_key) = key {
            if !body.is_empty() { self.cipher.encrypt(body, actual_key, nonce_out, tag_out).map_err(|_| ClientError::EncryptionFailed)?; }
        }
        Ok(req.body(Bytes::copy_from_slice(body)))
    }

    fn set_authentication_headers(
        &self,
        req: reqwest::RequestBuilder,
        session_ref: &SessionRefBuffer,
        tag: Option<&TagBuffer>,
        nonce: Option<&NonceBuffer>,
    ) -> Result<reqwest::RequestBuilder, ClientError> {
        let mut headers = HeaderMap::new();
        let mut session_ref_str_buf = [0u8; 36];
        converters::b16uuid_to_string(session_ref, &mut session_ref_str_buf);
        let session_ref_str = str::from_utf8(&session_ref_str_buf).map_err(|_| ClientError::HeaderCompositionFailed)?;
        headers.insert(HeaderName::from_static(SESSION_REF_HEADER_NAME), HeaderValue::from_str(session_ref_str).map_err(|_| ClientError::HeaderCompositionFailed)?);
        if let Some(actual_tag) = tag {
            self.set_b64_header::<TAG_BUFFER_B64_LEN>(&mut headers, REQUEST_TAG_HEADER_NAME, actual_tag)?;
        }
        if let Some(actual_nonce) = nonce {
            self.set_b64_header::<NONCE_BUFFER_B64_LEN>(&mut headers, REQUEST_NONCE_HEADER_NAME, actual_nonce)?;
        }
        Ok(req.headers(headers))
    }

    fn set_handshake_headers(
        &self,
        req: reqwest::RequestBuilder,
        client_pubkey: &KeyBuffer,
        tag: &TagBuffer,
        nonce: &NonceBuffer,
    ) -> Result<reqwest::RequestBuilder, ClientError> {
        let mut headers = HeaderMap::new();
        self.set_b64_header::<KEY_BUFFER_B64_LEN>(&mut headers, HANDSHAKE_PUBKEY_HEADER_NAME, client_pubkey)?;
        self.set_b64_header::<NONCE_BUFFER_B64_LEN>(&mut headers, REQUEST_NONCE_HEADER_NAME, nonce)?;
        self.set_b64_header::<TAG_BUFFER_B64_LEN>(&mut headers, REQUEST_TAG_HEADER_NAME, tag)?;
        Ok(req.headers(headers))
    }

    fn set_b64_header<const S: usize>(&self, headers: &mut HeaderMap, name: &'static str, data: &[u8]) -> Result<(), ClientError> {
        let mut b64_buf = [0u8; S];
        let b64_buf_len = converters::bytes_to_b64(data, &mut b64_buf).map_err(|_| ClientError::HeaderCompositionFailed)?;
        let b64_str = str::from_utf8(&b64_buf[..b64_buf_len]).map_err(|_| ClientError::HeaderCompositionFailed)?;
        headers.insert(HeaderName::from_static(name), HeaderValue::from_str(b64_str).map_err(|_| ClientError::HeaderCompositionFailed)?);
        Ok(())
    }

    async fn generate_handshake_key(&self, key: &mut KeyBuffer, client_pubkey: &mut KeyBuffer) -> Result<(), ClientError> {
        let mut server_pubkey_buf: KeyBuffer = [0u8; 32];
        self.pubkey(&mut server_pubkey_buf).await?;
        let (client_pubkey_temp, secret) = self.ephemeral_engine.generate_x25519_keypair();
        client_pubkey[..KEY_BUFFER_LEN].copy_from_slice(client_pubkey_temp.as_ref());
        let shared_secret = self.ephemeral_engine.generate_shared_key(&x25519_dalek::PublicKey::from(server_pubkey_buf), secret);
        key[..KEY_BUFFER_LEN].copy_from_slice(&self
            .ephemeral_engine
            .derive_hkdf_key(&shared_secret)
            .map_err(|_| ClientError::EncryptionFailed)?[..],
        );
        Ok(())
    }

    async fn handle_response<E: serde::de::DeserializeOwned + Default, const SE: usize>(&self, res: reqwest::Response, key: Option<&KeyBuffer>, out: &mut E) -> Result<(), ClientError> {
        self.unwrap_http_errors(res.status().as_u16())?;
        let decrypt = res.headers().contains_key(RESPONSE_TAG_HEADER_NAME) && res.headers().contains_key(RESPONSE_NONCE_HEADER_NAME) && key.is_some();
        let mut res_tag_buf: TagBuffer = [0u8; TAG_BUFFER_LEN];
        let mut res_nonce_buf: NonceBuffer = [0u8; NONCE_BUFFER_LEN];
        if decrypt {
            let res_tag_header = res.headers().get(RESPONSE_TAG_HEADER_NAME).ok_or(ClientError::DecryptionFailed)?;
            let res_nonce_header = res.headers().get(RESPONSE_NONCE_HEADER_NAME).ok_or(ClientError::DecryptionFailed)?;
            converters::b64_to_bytes(res_tag_header.as_bytes(), &mut res_tag_buf)?;
            converters::b64_to_bytes(res_nonce_header.as_bytes(), &mut res_nonce_buf)?;
        }

        let body_buf: &mut [u8] = &mut BytesMut::from(res.bytes().await.map_err(|_| ClientError::NetworkError)?);
        if body_buf.len() > SE { return Err(ClientError::BufferTooSmall); }
        if let Some(actual_key) = key { self.cipher.decrypt(body_buf, actual_key, &res_tag_buf, &res_nonce_buf).map_err(|_| ClientError::DecryptionFailed)?; }
        converters::bytes_to_body::<E>(body_buf, out)?;

        Ok(())
    }

    async fn unauthenticated_request(&self, path: &PathBuffer, out: &mut [u8]) -> Result<usize, ClientError> {
        let credentials = self.credentials.current()?;

        let mut url_buf = [0u8; HTTP_PATH_BUFFER_LEN];
        new_http_url(&credentials.addr, &credentials.port, path, credentials.secured, &mut url_buf)?;
        let url_buf_len = url_buf.iter().position(|&byte| byte == 0).unwrap_or(HTTP_PATH_BUFFER_LEN);
        let url = str::from_utf8(&url_buf[..url_buf_len]).map_err(|_| ClientError::UrlError)?;

        let res = self.http
                      .get(url)
                      .send()
                      .await
                      .map_err(|_| ClientError::NetworkError)?;
        self.unwrap_http_errors(res.status().as_u16())?;
        let res_buf = res.bytes().await.map_err(|_| ClientError::NetworkError)?;
        if res_buf.len() > out.len() { return Err(ClientError::BufferTooSmall); };
        out[..res_buf.len()].copy_from_slice(&res_buf);
        Ok(res_buf.len())
    }
}
