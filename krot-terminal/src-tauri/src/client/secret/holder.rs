use crate::client::error::ClientError;
use crate::client::secret::credentials::Credentials;
use crate::client::secret::session::Session;
use crate::security::keystore::{ApplicationKeystore, CredentialsKeystore, Keystore, SessionKeystore};
use std::sync::{Arc, RwLock};

pub trait HolderKeystoreBridge: Default + Clone + Copy {
    fn read(keystore: &ApplicationKeystore, item: &mut Self) -> Result<(), ClientError>;

    fn store(keystore: &ApplicationKeystore, item: &Self) -> Result<(), ClientError>;

    fn drop(keystore: &ApplicationKeystore) -> Result<(), ClientError>;

    fn not_found_error() -> ClientError;
}

impl HolderKeystoreBridge for Session {
    fn read(keystore: &ApplicationKeystore, item: &mut Self) -> Result<(), ClientError> {
        keystore.session_keystore.read(SessionKeystore::IDX, item).map_err(|_| Self::not_found_error())
    }

    fn store(keystore: &ApplicationKeystore, item: &Self) -> Result<(), ClientError> {
        keystore.session_keystore.store(SessionKeystore::IDX, item).map_err(|_| Self::not_found_error())
    }

    fn drop(keystore: &ApplicationKeystore) -> Result<(), ClientError> {
        keystore.session_keystore.drop(SessionKeystore::IDX).map_err(|_| Self::not_found_error())
    }

    fn not_found_error() -> ClientError {
        ClientError::SessionNotFound
    }
}

impl HolderKeystoreBridge for Credentials {
    fn read(keystore: &ApplicationKeystore, item: &mut Self) -> Result<(), ClientError> {
        keystore.credentials_keystore.read(CredentialsKeystore::IDX, item).map_err(|_| Self::not_found_error())
    }

    fn store(keystore: &ApplicationKeystore, item: &Self) -> Result<(), ClientError> {
        keystore.credentials_keystore.store(CredentialsKeystore::IDX, item).map_err(|_| Self::not_found_error())
    }

    fn drop(keystore: &ApplicationKeystore) -> Result<(), ClientError> {
        keystore.credentials_keystore.drop(CredentialsKeystore::IDX).map_err(|_| Self::not_found_error())
    }

    fn not_found_error() -> ClientError {
        ClientError::CredentialsNotFound
    }
}

pub struct Holder<T: HolderKeystoreBridge> {
    keystore: Arc<ApplicationKeystore>,
    cached: RwLock<Option<T>>,
}

impl<T: HolderKeystoreBridge> Holder<T> {
    pub fn new(keystore: Arc<ApplicationKeystore>) -> Self {
        let mut item = T::default();
        let cached = T::read(&keystore, &mut item).ok().map(|_| item);
        Self { keystore, cached: RwLock::new(cached) }
    }

    pub fn current(&self) -> Result<T, ClientError> {
        self.cached.read().unwrap().ok_or(T::not_found_error())
    }

    pub fn store(&self, item: T) -> Result<(), ClientError> {
        T::store(&self.keystore, &item)?;
        *self.cached.write().unwrap() = Some(item);
        Ok(())
    }

    pub fn clear(&self) {
        let _ = T::drop(&self.keystore);
        *self.cached.write().unwrap() = None;
    }
}
