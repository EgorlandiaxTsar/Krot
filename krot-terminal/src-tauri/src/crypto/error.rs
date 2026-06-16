#[derive(Debug, PartialEq, Eq)]
pub enum CryptoError {
    InvalidKeyLength,
    InvalidNonceLength,
    InvalidTagLength,
    BufferTooSmall,
    EncryptionFailed,
    DecryptionFailed,
    DerivationFailed,
}
