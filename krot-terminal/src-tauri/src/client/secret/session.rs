use crate::client::error::ClientError;
use crate::client::secret::holder::Holder;
use crate::client::utils;

pub const SESSION_SIZE: usize = size_of::<Session>();

#[repr(C)]
#[derive(Default, Debug, Copy, Clone)]
pub struct Session {
    pub id: [u8; 16],
    pub reference_key: [u8; 16],
    pub encryption_key: [u8; 32],
    pub expiration: i64,
}

pub type SessionHolder = Holder<Session>;

impl SessionHolder {
    pub fn check(&self) -> Result<(), ClientError> {
        let session = self.current()?;
        let timestamp = utils::timestamp()?;
        let diff = session.expiration - timestamp;
        if diff <= 0 {
            self.clear();
            return Err(ClientError::SessionExpired);
        }
        if diff < 1000 * 60 * 5 { return Err(ClientError::SessionAboutToExpire); }
        Ok(())
    }
}
