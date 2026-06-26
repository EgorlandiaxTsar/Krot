use serde::Serialize;

#[derive(Debug, PartialEq, Eq, Serialize)]
pub enum CryptoError {
    InvalidKeyLength,
    InvalidNonceLength,
    InvalidTagLength,
    BufferTooSmall,
    EncryptionFailed,
    DecryptionFailed,
    DerivationFailed,
}
