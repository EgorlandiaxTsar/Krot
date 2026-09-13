use crate::client::api::model::common::ResponseMetadata;
use serde::Serialize;

#[derive(Debug, PartialEq, Eq, Serialize)]
pub enum ClientError {
    // Boxing because stack size would get too large
    BadRequest(Box<ResponseMetadata>),
    Unauthorized,
    Forbidden(Box<ResponseMetadata>),
    NotFound(Box<ResponseMetadata>),
    Conflict(Box<ResponseMetadata>),
    InternalServerError(Box<ResponseMetadata>),
    ServiceUnavailable,

    UnauthenticatedBadRequest,
    UnauthenticatedForbidden,
    UnauthenticatedNotFound,
    UnauthenticatedConflict,
    UnauthenticatedInternalServerError,

    CredentialsNotFound,
    SessionNotFound,
    SessionAboutToExpire,
    SessionExpired,

    DecryptionFailed,
    EncryptionFailed,
    ConversionFailed,
    BodyCompositionFailed,
    BodyParseFailed,
    HeaderCompositionFailed,
    NonceGenerationFailed,
    UrlError,

    HelloFailed,
    PubkeyFetchFailed,

    NetworkError,
    TimestampError,
    BufferTooSmall,

    WsHandshakeFailed,
    WsConnectionFailed,
    WsProtocolViolation,
    WsNotConnected,
    WsSendFailed,
}
