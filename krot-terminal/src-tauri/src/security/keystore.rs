use crate::client::confidential::{Credentials, Session, CREDENTIALS_SIZE, SESSION_SIZE};
use crate::client::types::converters::{b64_to_bytes, bytes_to_b64};
use crate::security::error::SecurityError;
use crate::U8_STRINGS;
use keyring::{Entry, Error};

const KEYSTORE_NAME: &str = "KrotTerminalKeystore";
const SESSION_B64_SIZE: usize = 4 * ((SESSION_SIZE + 2) / 3);
const CREDENTIALS_B64_SIZE: usize = 4 * ((CREDENTIALS_SIZE + 2) / 3);

pub trait FromByteKeypair {
    fn from_byte_keypair(key: &u8, idx: &u8) -> Result<Entry, Error>;

    fn from_idx(idx: &u8) -> Result<Entry, Error>;
}

impl FromByteKeypair for Entry {
    fn from_byte_keypair(key: &u8, idx: &u8) -> Result<Entry, Error> {
        Entry::new(U8_STRINGS[*key as usize], U8_STRINGS[*idx as usize])
    }

    fn from_idx(idx: &u8) -> Result<Entry, Error> {
        Entry::new(U8_STRINGS[*idx as usize], KEYSTORE_NAME)
    }
}

pub trait Keystore<T: ?Sized> {
    fn store(&self, idx: u8, data: &T) -> Result<(), SecurityError>;

    fn read(&self, idx: u8, out: &mut T) -> Result<(), SecurityError>;

    fn drop(&self, idx: u8) -> Result<(), SecurityError> {
        let entry = Entry::from_idx(&idx).map_err(|_| SecurityError::DeleteFailed)?;
        entry
            .delete_credential()
            .map_err(|_| SecurityError::DeleteFailed)?;
        Ok(())
    }
}

pub trait B64Keystore<T: ?Sized> {
    fn store_b64<const S: usize>(&self, idx: u8, data: &T) -> Result<(), SecurityError>;

    fn read_b64<const S: usize>(&self, idx: u8, out: &mut T) -> Result<(), SecurityError>;
}

pub struct BytesKeystore;

impl BytesKeystore {
    pub const IDX: u8 = 0;
}

impl Keystore<[u8]> for BytesKeystore {
    fn store(&self, idx: u8, data: &[u8]) -> Result<(), SecurityError> {
        let entry = Entry::from_idx(&idx).map_err(|_| SecurityError::WriteFailed)?;
        entry.set_secret(data).map_err(|e| {
            if matches!(e, Error::Ambiguous(..)) {
                return SecurityError::AlreadyExists;
            }
            SecurityError::WriteFailed
        })?;
        Ok(())
    }

    fn read(&self, idx: u8, out: &mut [u8]) -> Result<(), SecurityError> {
        let entry = Entry::from_idx(&idx).map_err(|_| SecurityError::FetchFailed)?;
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
}

impl B64Keystore<[u8]> for BytesKeystore {
    fn store_b64<const S: usize>(&self, idx: u8, data: &[u8]) -> Result<(), SecurityError> {
        let mut converted_bytes_buf = [0u8; S];
        bytes_to_b64(data, &mut converted_bytes_buf[..]).map_err(|_| SecurityError::WriteFailed)?;
        self.store(idx, &converted_bytes_buf)
    }

    fn read_b64<const S: usize>(&self, idx: u8, out: &mut [u8]) -> Result<(), SecurityError> {
        let mut data_buf = [0u8; S];
        self.read(idx, &mut data_buf[..])?;
        b64_to_bytes(&data_buf, &mut out[..]).map_err(|_| SecurityError::WriteFailed)?;
        Ok(())
    }
}

pub struct SessionKeystore {
    bytes_keystore: BytesKeystore,
}

impl SessionKeystore {
    pub const IDX: u8 = 1;
}

impl Keystore<Session> for SessionKeystore {
    fn store(&self, idx: u8, data: &Session) -> Result<(), SecurityError> {
        let raw_bytes: &[u8; SESSION_SIZE] = unsafe { std::mem::transmute(data) };
        self.bytes_keystore.store_b64::<SESSION_B64_SIZE>(idx, raw_bytes)
    }

    fn read(&self, idx: u8, out: &mut Session) -> Result<(), SecurityError> {
        let mut buf = [0u8; SESSION_SIZE];
        self.bytes_keystore.read_b64::<SESSION_B64_SIZE>(idx, &mut buf)?;
        *out = unsafe { std::mem::transmute(buf) };
        Ok(())
    }
}

pub struct CredentialsKeystore {
    bytes_keystore: BytesKeystore,
}

impl CredentialsKeystore {
    pub const IDX: u8 = 2;
}

impl Keystore<Credentials> for CredentialsKeystore {
    fn store(&self, idx: u8, data: &Credentials) -> Result<(), SecurityError> {
        let raw_bytes: &[u8; CREDENTIALS_SIZE] = unsafe { std::mem::transmute(data) };
        self.bytes_keystore.store_b64::<CREDENTIALS_B64_SIZE>(idx, raw_bytes)
    }

    fn read(&self, idx: u8, out: &mut Credentials) -> Result<(), SecurityError> {
        let mut buf = [0u8; CREDENTIALS_SIZE];
        self.bytes_keystore.read_b64::<CREDENTIALS_B64_SIZE>(idx, &mut buf)?;
        *out = unsafe { std::mem::transmute(buf) };
        Ok(())
    }
}

pub struct ApplicationKeystore {
    pub session_keystore: SessionKeystore,
    pub credentials_keystore: CredentialsKeystore,
}

impl ApplicationKeystore {
    pub fn new() -> Self {
        ApplicationKeystore {
            session_keystore: SessionKeystore {
                bytes_keystore: BytesKeystore,
            },
            credentials_keystore: CredentialsKeystore {
                bytes_keystore: BytesKeystore,
            },
        }
    }
}
