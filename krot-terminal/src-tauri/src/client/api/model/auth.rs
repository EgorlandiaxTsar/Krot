use crate::client::api::model::common::{RequestMetadata, ResponseMetadata};
use crate::client::api::model::size::{i64_cost, uuid_cost, varchar_cost, JsonSized, MemorySized, BRACES_OVERHEAD, FIELD_OVERHEAD};
use crate::client::types::serde::{deserialize, serialize};
use serde::{Deserialize, Serialize};

// Models
#[repr(C)]
#[derive(Default, Debug, Copy, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct AuthenticationCredentials {
    #[serde(deserialize_with = "deserialize::uuid")]
    pub session_id: [u8; 16],
    #[serde(rename = "sessionReference", deserialize_with = "deserialize::uuid")]
    pub session_ref: [u8; 16],
    #[serde(rename = "key", deserialize_with = "deserialize::b32_encryption_key")]
    pub encryption_key: [u8; 32],
    #[serde(rename = "expirationTimestamp", )]
    pub expiration: i64,
}

// Request Models
#[repr(C)]
#[derive(Debug, Copy, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ApiAuthenticationRequest {
    #[serde(serialize_with = "serialize::varchar")]
    pub identifier: [u8; 128],
    #[serde(serialize_with = "serialize::varchar")]
    pub password: [u8; 128],
    pub timestamp: i64,
    #[serde(serialize_with = "serialize::varchar")]
    pub target: [u8; 6],
}

#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct WsAuthenticationRequest {
    pub metadata: RequestMetadata,
}


#[repr(C)]
#[derive(Debug, Copy, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct DisconnectRequest {
    pub metadata: RequestMetadata,
}

// Response Models
#[repr(C)]
#[derive(Default, Debug, Copy, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct AuthenticationResponse {
    pub metadata: ResponseMetadata,
    pub data: AuthenticationCredentials,
}

// Implements
impl ApiAuthenticationRequest {
    pub fn new(identifier: [u8; 128], password: [u8; 128]) -> Self {
        let mut req = Self::default();
        req.identifier = identifier;
        req.password = password;
        req
    }
}

// Default Implements
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

// JSON Size Implements
impl JsonSized for AuthenticationCredentials {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + varchar_cost(44) + FIELD_OVERHEAD
        + i64_cost() + FIELD_OVERHEAD;
}

impl JsonSized for ApiAuthenticationRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + varchar_cost(32) + FIELD_OVERHEAD
        + varchar_cost(32) + FIELD_OVERHEAD
        + i64_cost() + FIELD_OVERHEAD
        + varchar_cost(6) + FIELD_OVERHEAD;
}

impl JsonSized for WsAuthenticationRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD;
}

impl JsonSized for DisconnectRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD;
}

impl JsonSized for AuthenticationResponse {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + ResponseMetadata::JSON_SIZE + FIELD_OVERHEAD
        + AuthenticationCredentials::JSON_SIZE + FIELD_OVERHEAD;
}

// Memory Size Implements
impl MemorySized for AuthenticationCredentials {}
impl MemorySized for ApiAuthenticationRequest {}
impl MemorySized for WsAuthenticationRequest {}
impl MemorySized for DisconnectRequest {}
impl MemorySized for AuthenticationResponse {}
