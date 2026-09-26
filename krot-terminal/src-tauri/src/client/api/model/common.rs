use crate::client::api::model::size::{bool_cost, i32_cost, i64_cost, uuid_cost, varchar_cost, JsonSized, MemorySized, BRACES_OVERHEAD, FIELD_OVERHEAD};
use crate::client::types::serde::{deserialize, serialize};
use crate::client::utils::new_model;

pub type IdList = Vec<[u8; 16]>;

// Models
new_model! {
    pub struct RequestMetadata {
        #[serde(rename = "sessionId", serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub session_id: [u8; 16],
        pub timestamp: i64,
    }
}

new_model! {
    pub struct ResponseMetadata {
        pub code: u16,
        #[serde(rename = "message", serialize_with="serialize::varchar", deserialize_with = "deserialize::varchar")]
        pub message: [u8; 2048],
        pub timestamp: i64,
        pub success: bool,
    }
}

new_model! {
    pub struct RangeFilter {
        #[serde(skip_serializing_if = "Option::is_none")]
        pub min: Option<i64>,
        #[serde(skip_serializing_if = "Option::is_none")]
        pub max: Option<i64>,
    }
}

new_model! {
    pub struct PaginationOptions {
        pub page: i32,
        pub limit: i32,
        #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::varchar_opt", deserialize_with = "deserialize::varchar_opt")]
        pub order: Option<[u8; 128]>,
    }
}

new_model! {
    pub struct Pagination {
        pub limit: i32,
        pub items: i32,
        pub pages: i32,
        pub page: i32,
        pub from: i32,
        pub to: i32,
        pub start: bool,
        pub end: bool,
    }
}

// Response Models
new_model! {
    pub struct BlankResponse {
        pub metadata: ResponseMetadata,
    }
}

// Implements
impl RequestMetadata {
    pub fn new(session_id: [u8; 16]) -> Self {
        let mut metadata = Self::default();
        metadata.session_id = session_id;
        metadata
    }
}

impl JsonSized for RequestMetadata {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + i64_cost() + FIELD_OVERHEAD;
}

// JSON Size Implements
impl JsonSized for ResponseMetadata {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + i32_cost() + FIELD_OVERHEAD
        + varchar_cost(512) + FIELD_OVERHEAD
        + i64_cost() + FIELD_OVERHEAD
        + bool_cost() + FIELD_OVERHEAD;
}

impl JsonSized for RangeFilter {
    const JSON_SIZE: usize = BRACES_OVERHEAD + 2 * (i64_cost() + FIELD_OVERHEAD);
}

impl JsonSized for PaginationOptions {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + i32_cost() + FIELD_OVERHEAD
        + i32_cost() + FIELD_OVERHEAD
        + varchar_cost(64) + FIELD_OVERHEAD;
}

impl JsonSized for Pagination {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + 6 * (i32_cost() + BRACES_OVERHEAD)
        + 2 * (bool_cost() + BRACES_OVERHEAD);
}

impl JsonSized for BlankResponse {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD;
}

// Memory Size Implements
impl MemorySized for RequestMetadata {}
impl MemorySized for ResponseMetadata {}
impl MemorySized for RangeFilter {}
impl MemorySized for PaginationOptions {}
impl MemorySized for Pagination {}
impl MemorySized for BlankResponse {}
