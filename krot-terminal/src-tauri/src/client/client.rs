use crate::client::api::gateway::ApiGateway;
use crate::client::api::model::common::AuthenticationCredentials;
use crate::client::error::ClientError;
use crate::client::secret::credentials::CredentialsHolder;
use crate::client::secret::session::{Session, SessionHolder};
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
        Self {
            session: session_holder_arc.clone(),
            credentials: credentials_holder_arc.clone(),
            api: ApiGateway::new(session_holder_arc.clone(), credentials_holder_arc.clone()),
            ws: WsGateway::new(session_holder_arc.clone(), credentials_holder_arc.clone()),
            refresh_lock: Arc::from(tokio::sync::Mutex::new(())),
        }
    }

    pub fn authenticated(&self) -> bool { self.session.current().is_ok() }

    pub async fn api(&self) -> Result<&ApiGateway, ClientError> {
        self.check_session().await?;
        Ok(&self.api)
    }

    pub async fn ws(&self) -> Result<&WsGateway, ClientError> {
        self.check_session().await?;
        Ok(&self.ws)
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

                let mut credentials = AuthenticationCredentials::default();
                self.api.authenticate(&mut credentials).await?;
                self.session.store(Session {
                    id: credentials.session_id,
                    reference_key: credentials.session_ref,
                    encryption_key: credentials.encryption_key,
                    expiration: credentials.expiration,
                })?;
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
