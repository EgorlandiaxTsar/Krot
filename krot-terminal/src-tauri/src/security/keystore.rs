use crate::client::confidential::{Credentials, Session, CREDENTIALS_SIZE, SESSION_SIZE};
use crate::security::error::SecurityError;
use crate::U8_STRINGS;
use keyring_core::{Entry, Error};

pub trait FromByteKeypair {
    fn from_byte_keypair(key: &u8, idx: &u8) -> Result<Entry, Error>;
}

impl FromByteKeypair for Entry {
    fn from_byte_keypair(key: &u8, idx: &u8) -> Result<Entry, Error> {
        Entry::new(U8_STRINGS[*key as usize], U8_STRINGS[*idx as usize])
    }
}

pub trait Keystore<T: ?Sized> {
    fn store(&self, idx: u8, data: &T) -> Result<(), SecurityError>;

    fn read(&self, idx: u8, out: &mut T) -> Result<(), SecurityError>;

    fn drop(&self, idx: u8) -> Result<(), SecurityError> {
        let entry = Entry::from_byte_keypair(Self::keystore_id(), &idx)
            .map_err(|_| SecurityError::DeleteFailed)?;
        entry
            .delete_credential()
            .map_err(|_| SecurityError::DeleteFailed)?;
        Ok(())
    }

    fn keystore_id() -> &'static u8 {
        &0u8
    }
}

pub struct BytesKeystore;

impl Keystore<[u8]> for BytesKeystore {
    fn store(&self, idx: u8, data: &[u8]) -> Result<(), SecurityError> {
        let entry = Entry::from_byte_keypair(Self::keystore_id(), &idx)
            .map_err(|_| SecurityError::WriteFailed)?;
        entry.set_secret(data).map_err(|e| {
            if matches!(e, Error::Ambiguous(..)) {
                return SecurityError::AlreadyExists;
            }
            SecurityError::WriteFailed
        })?;
        Ok(())
    }

    fn read(&self, idx: u8, out: &mut [u8]) -> Result<(), SecurityError> {
        let entry = Entry::from_byte_keypair(&Self::keystore_id(), &idx)
            .map_err(|_| SecurityError::FetchFailed)?;
        out.copy_from_slice(
            entry
                .get_secret()
                .map_err(|e| {
                    if matches!(e, Error::NoEntry) {
                        return SecurityError::NotFound;
                    }
                    SecurityError::FetchFailed
                })?
                .as_slice(),
        );
        Ok(())
    }

    fn keystore_id() -> &'static u8 {
        &1u8
    }
}

pub struct SessionKeystore {
    bytes_keystore: BytesKeystore,
}

impl Keystore<Session> for SessionKeystore {
    fn store(&self, idx: u8, data: &Session) -> Result<(), SecurityError> {
        let raw_bytes: &[u8; SESSION_SIZE] = unsafe { std::mem::transmute(data) };
        self.bytes_keystore.store(idx, raw_bytes)
    }

    fn read(&self, idx: u8, out: &mut Session) -> Result<(), SecurityError> {
        let mut buf = [0u8; SESSION_SIZE];
        self.bytes_keystore.read(idx, &mut buf)?;
        *out = unsafe { std::mem::transmute(buf) };
        Ok(())
    }

    fn keystore_id() -> &'static u8 {
        &2u8
    }
}

pub struct CredentialsKeystore {
    bytes_keystore: BytesKeystore,
}

impl Keystore<Credentials> for CredentialsKeystore {
    fn store(&self, idx: u8, data: &Credentials) -> Result<(), SecurityError> {
        let raw_bytes: &[u8; CREDENTIALS_SIZE] = unsafe { std::mem::transmute(data) };
        self.bytes_keystore.store(idx, raw_bytes)
    }

    fn read(&self, idx: u8, out: &mut Credentials) -> Result<(), SecurityError> {
        let mut buf = [0u8; CREDENTIALS_SIZE];
        self.bytes_keystore.read(idx, &mut buf)?;
        *out = unsafe { std::mem::transmute(buf) };
        Ok(())
    }

    fn keystore_id() -> &'static u8 {
        &3u8
    }
}

pub struct ApplicationKeystore {
    pub bytes_keystore: BytesKeystore,
    pub session_keystore: SessionKeystore,
    pub credentials_keystore: CredentialsKeystore,
}

impl ApplicationKeystore {
    pub fn new() -> Self {
        ApplicationKeystore {
            bytes_keystore: BytesKeystore,
            session_keystore: SessionKeystore {
                bytes_keystore: BytesKeystore,
            },
            credentials_keystore: CredentialsKeystore {
                bytes_keystore: BytesKeystore,
            },
        }
    }
}
