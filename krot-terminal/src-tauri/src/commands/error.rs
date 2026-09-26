use crate::client::api::model::common::ResponseMetadata;
use crate::client::error::ClientError;
use serde::Serialize;

#[derive(Debug, Serialize)]
#[serde(tag = "type", content = "details")]
pub enum CommandError {
    // Internal
    Argument(String),
    Internal(String),

    // Login Flow
    Hello,
    PubkeyRetrieval(ClientError),
    Authentication(ClientError),

    // API
    Endpoint(Box<ResponseMetadata>),

    // Metadata
    Session(ClientError),
    Credentials(ClientError),
}

impl From<ClientError> for CommandError {
    fn from(err: ClientError) -> Self {
        match err {
            ClientError::BadRequest(r) |
            ClientError::Forbidden(r) |
            ClientError::NotFound(r) |
            ClientError::Conflict(r) |
            ClientError::InternalServerError(r) => { CommandError::Endpoint(r) }
            other => CommandError::Internal(format!("{other:?}")),
        }
    }
}
