use crate::client::types::serde::{deserialize, serialize};
use crate::client::utils;
use serde::{Deserialize, Serialize};

#[repr(C)]
#[derive(Default, Debug, Copy, Clone, Deserialize)]
pub struct ResponseMetadata {
    pub code: u16,
    // pub message: [u8; 2048] // We ignore message, since it's developer purpose, not shown to the user, but eating 2KB of memory
    pub timestamp: i64,
    pub success: bool,
}

#[repr(C)]
#[derive(Debug, Copy, Clone, Serialize)]
pub struct RequestMetadata {
    #[serde(rename = "sessionId", serialize_with = "serialize::uuid")]
    pub session_id: [u8; 16],
    pub timestamp: i64,
}

impl RequestMetadata {
    pub fn new(session_id: [u8; 16]) -> Self {
        let mut metadata = Self::default();
        metadata.session_id = session_id;
        metadata
    }
}

impl Default for RequestMetadata {
    fn default() -> Self {
        Self {
            session_id: [0u8; 16],
            timestamp: utils::timestamp().unwrap_or(0),
        }
    }
}

#[repr(C)]
#[derive(Default, Debug, Copy, Clone, Deserialize)]
pub struct AuthenticationCredentials {
    #[serde(rename = "sessionId", deserialize_with = "deserialize::uuid")]
    pub session_id: [u8; 16],
    #[serde(rename = "sessionReference", deserialize_with = "deserialize::uuid")]
    pub session_ref: [u8; 16],
    #[serde(rename = "key", deserialize_with = "deserialize::b32_encryption_key")]
    pub encryption_key: [u8; 32],
    #[serde(rename = "expirationTimestamp", )]
    pub expiration: i64,
}
