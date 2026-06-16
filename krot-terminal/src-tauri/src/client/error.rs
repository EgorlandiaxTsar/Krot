#[derive(Debug, PartialEq, Eq)]
pub enum ClientError {
    BadRequest,
    Unauthorized,
    Forbidden,
    NotFound,
    Conflict,
    InternalServerError,
    ServiceUnavailable,

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
}
