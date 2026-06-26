use crate::client::types::serde_helpers::{deserialize, serialize};
use serde::{Deserialize, Serialize};

pub const METADATA_SIZE: usize = size_of::<ResponseMetadata>();
pub const AUTHENTICATION_CREDENTIALS_SIZE: usize = size_of::<AuthenticationCredentials>();

#[repr(C)]
#[derive(Default, Debug, Copy, Clone, Deserialize)]
pub struct ResponseMetadata {
    pub code: u16,
    // pub message: [u8; 2048] // We ignore message, since it's developer purpose, not shown to the user, but eating 2KB of memory
    pub timestamp: i64,
    pub success: bool,
}

#[repr(C)]
#[derive(Default, Debug, Copy, Clone, Serialize)]
pub struct RequestMetadata {
    #[serde(rename = "sessionId", serialize_with = "serialize::uuid")]
    pub session_id: [u8; 16],
    pub timestamp: i64,
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
