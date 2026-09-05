use crate::client::api::model::common::RequestMetadata;
use crate::client::types::serde::serialize;
use serde::Serialize;

#[repr(C)]
#[derive(Debug, Copy, Clone, Serialize)]
pub struct ApiAuthenticationRequest {
    #[serde(serialize_with = "serialize::varchar")]
    pub identifier: [u8; 128],
    #[serde(serialize_with = "serialize::varchar")]
    pub password: [u8; 128],
    pub timestamp: i64,
    #[serde(serialize_with = "serialize::varchar")]
    pub target: [u8; 6],
}

impl ApiAuthenticationRequest {
    pub fn new(identifier: [u8; 128], password: [u8; 128]) -> Self {
        let mut req = Self::default();
        req.identifier = identifier;
        req.password = password;
        req
    }
}


impl Default for ApiAuthenticationRequest {
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
#[derive(Debug, Clone, Serialize)]
pub struct WsAuthenticationRequest {
    pub metadata: RequestMetadata,
}

#[repr(C)]
#[derive(Debug, Copy, Clone, Serialize)]
pub struct DisconnectRequest {
    pub metadata: RequestMetadata,
}

impl Default for DisconnectRequest {
    fn default() -> Self {
        Self {
            metadata: RequestMetadata {
                session_id: [0u8; 16],
                timestamp: std::time::SystemTime::now()
                    .duration_since(std::time::UNIX_EPOCH)
                    .unwrap()
                    .as_millis() as i64,
            }
        }
    }
}
