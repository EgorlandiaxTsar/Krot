use crate::crypto::error::CryptoError;
use hkdf::Hkdf;
use sha2::Sha256;
use x25519_dalek::{EphemeralSecret, PublicKey, SharedSecret};

pub struct EphemeralEngine;

impl EphemeralEngine {
    pub fn generate_x25519_keypair(&self) -> (PublicKey, EphemeralSecret) {
        let secret = EphemeralSecret::random();
        (PublicKey::from(&secret), secret)
    }

    pub fn generate_shared_key(&self, pubkey: &PublicKey, secret: EphemeralSecret) -> SharedSecret {
        secret.diffie_hellman(&pubkey)
    }

    pub fn derive_hkdf_key(&self, shared_secret: &SharedSecret) -> Result<[u8; 32], CryptoError> {
        let mut buf = [0u8; 32];
        let hk = Hkdf::<Sha256>::new(None, shared_secret.as_bytes());
        hk.expand(b"handshake-v1", buf.as_mut_slice())
          .map_err(|_| CryptoError::DerivationFailed)?;
        Ok(buf)
    }
}
