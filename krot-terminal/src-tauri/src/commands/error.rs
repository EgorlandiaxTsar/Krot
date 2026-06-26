use crate::client::error::ClientError;
use crate::security::error::SecurityError;
use serde::Serialize;

#[derive(Debug, Serialize)]
#[serde(tag = "type", content = "details")]
pub enum CommandError {
    ClientAuthenticationFailed(ClientError),
    ClientCredentialsUpdateFailed(ClientError),
    ClientDisconnectFailed(ClientError),
    ClientHelloFailed(ClientError),

    CredentialsNotFound,
    SessionNotFound,

    KeystoreAccessFailed(SecurityError),

    ArgumentError(String),
    InternalError(String),
}
