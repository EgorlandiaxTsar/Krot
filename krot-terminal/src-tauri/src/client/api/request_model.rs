use crate::client::types::serde_helpers::serialize;
use serde::Serialize;

pub const AUTHENTICATION_REQUEST_SIZE: usize = size_of::<AuthenticationRequest>();
pub const DISCONNECT_REQUEST_SIZE: usize = size_of::<DisconnectRequest>();

#[repr(C)]
#[derive(Debug, Copy, Clone, Serialize)]
pub struct AuthenticationRequest {
    #[serde(serialize_with = "serialize::varchar")]
    pub identifier: [u8; 128],
    #[serde(serialize_with = "serialize::varchar")]
    pub password: [u8; 128],
    pub timestamp: i64,
    #[serde(serialize_with = "serialize::varchar")]
    pub target: [u8; 6],
}


impl Default for AuthenticationRequest {
    fn default() -> Self {
        let mut target_buf = [0u8; 6];
        target_buf[..4].copy_from_slice(b"USER");
        Self {
            identifier: [0u8; 128],
            password: [0u8; 128],
            timestamp: std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .unwrap()
                .as_millis() as i64,
            target: target_buf,
        }
    }
}

#[repr(C)]
#[derive(Debug, Copy, Clone, Serialize)]
pub struct DisconnectRequest {
    #[serde(serialize_with = "serialize::varchar")]
    pub session_id: [u8; 16],
    pub timestamp: i64,
}

impl Default for DisconnectRequest {
    fn default() -> Self {
        Self {
            session_id: [0u8; 16],
            timestamp: std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .unwrap()
                .as_millis() as i64,
        }
    }
}
