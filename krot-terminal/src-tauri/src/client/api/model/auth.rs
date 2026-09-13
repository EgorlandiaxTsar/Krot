use crate::client::api::model::common::{RequestMetadata, ResponseMetadata};
use crate::client::api::model::size::{i64_cost, uuid_cost, varchar_cost, JsonSized, MemorySized, BRACES_OVERHEAD, FIELD_OVERHEAD};
use crate::client::types::serde::{deserialize, serialize};
use crate::client::utils::res_model;
use crate::client::utils::{new_model, req_model};

// Models
res_model! {
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
}

// Request Models
req_model! {
    pub struct ApiAuthenticationRequest {
        #[serde(serialize_with = "serialize::varchar")]
        pub identifier: [u8; 128],
        #[serde(serialize_with = "serialize::varchar")]
        pub password: [u8; 128],
        pub timestamp: i64,
        #[serde(serialize_with = "serialize::varchar")]
        pub target: [u8; 6],
    }
}

req_model! {
    pub struct WsAuthenticationRequest {
        pub metadata: RequestMetadata,
    }
}

req_model! {
    pub struct DisconnectRequest {
        pub metadata: RequestMetadata,
    }
}

// Response Models
res_model! {
    pub struct AuthenticationResponse {
        pub metadata: ResponseMetadata,
        pub data: AuthenticationCredentials,
    }
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
