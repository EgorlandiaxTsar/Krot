use crate::crypto::error::CryptoError;
use chacha20poly1305::aead::inout::InOutBuf;
use chacha20poly1305::{aead::KeyInit, AeadInOut, ChaCha20Poly1305, Key, Nonce, Tag};

pub trait Encryptor {
    fn encrypt(
        &self,
        text: &mut [u8],
        key: &[u8],
        nonce: &[u8],
        tag_out: &mut [u8],
    ) -> Result<(), CryptoError>;
}

pub trait Decryptor {
    fn decrypt(
        &self,
        ciphertext: &mut [u8],
        key: &[u8],
        tag: &[u8],
        nonce: &[u8],
    ) -> Result<(), CryptoError>;
}

pub struct ChaCha20Poly1305Cipher;

impl Encryptor for ChaCha20Poly1305Cipher {
    fn encrypt(
        &self,
        text: &mut [u8],
        key: &[u8],
        nonce: &[u8],
        tag_out: &mut [u8],
    ) -> Result<(), CryptoError> {
        if key.len() != 32 {
            return Err(CryptoError::InvalidKeyLength);
        }
        if nonce.len() != 12 {
            return Err(CryptoError::InvalidNonceLength);
        }
        if tag_out.len() < 16 {
            return Err(CryptoError::InvalidTagLength);
        }
        let cp_key = Key::try_from(key).map_err(|_| CryptoError::EncryptionFailed)?;
        let cp_nonce = Nonce::try_from(nonce).map_err(|_| CryptoError::EncryptionFailed)?;
        let cp = ChaCha20Poly1305::new(&cp_key);
        tag_out[..16].copy_from_slice(
            &cp.encrypt_inout_detached(&cp_nonce, b"", InOutBuf::from(text))
               .map_err(|_| CryptoError::EncryptionFailed)?,
        );
        Ok(())
    }
}

impl Decryptor for ChaCha20Poly1305Cipher {
    fn decrypt(
        &self,
        ciphertext: &mut [u8],
        key: &[u8],
        tag: &[u8],
        nonce: &[u8],
    ) -> Result<(), CryptoError> {
        if key.len() != 32 {
            return Err(CryptoError::InvalidKeyLength);
        }
        if nonce.len() != 12 {
            return Err(CryptoError::InvalidNonceLength);
        }
        if tag.len() != 16 {
            return Err(CryptoError::InvalidTagLength);
        }
        let cp_key = Key::try_from(key).map_err(|_| CryptoError::DecryptionFailed)?;
        let cp = ChaCha20Poly1305::new(&cp_key);
        let cp_nonce = Nonce::try_from(nonce).map_err(|_| CryptoError::DecryptionFailed)?;
        let cp_tag = Tag::try_from(tag).map_err(|_| CryptoError::DecryptionFailed)?;
        cp.decrypt_inout_detached(&cp_nonce, b"", InOutBuf::from(ciphertext), &cp_tag)
          .map_err(|_| CryptoError::DecryptionFailed)?;
        Ok(())
    }
}
