use crate::client::api::model::common::{IdList, Pagination, PaginationOptions, RangeFilter, RequestMetadata, ResponseMetadata};
use crate::client::api::model::size::{bool_cost, i64_cost, uuid_cost, JsonSized, MemorySized, BRACES_OVERHEAD, FIELD_OVERHEAD, MAX_PAGE_LIMIT};
use crate::client::types::serde::{deserialize, serialize};
use serde::{Deserialize, Serialize};

// Models
#[repr(C)]
#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SessionModel {
    #[serde(deserialize_with = "deserialize::uuid")]
    pub id: [u8; 16],
    #[serde(deserialize_with = "deserialize::uuid")]
    pub owner_id: [u8; 16],
    pub is_owner_device: bool,
    pub valid_until: i64,
    pub created_at: i64,
}

// Request Models
#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct SessionFilter {
    pub metadata: RequestMetadata,
    #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::uuid_vec_opt")]
    pub ids: Option<IdList>,
    #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::uuid_opt")]
    pub owner_id: Option<[u8; 16]>,
    // Treated as optional — see note above.
    #[serde(skip_serializing_if = "Option::is_none")]
    pub valid_until_time: Option<RangeFilter>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub creation_time: Option<RangeFilter>,
    pub pagination: PaginationOptions,
}

// Response Models
#[repr(C)]
#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SessionFilterResponse {
    pub metadata: ResponseMetadata,
    pub data: Vec<SessionModel>,
    pub pagination: Pagination,
}

// JSON Size Implements
impl JsonSized for SessionModel {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + bool_cost() + FIELD_OVERHEAD
        + i64_cost() + FIELD_OVERHEAD
        + i64_cost() + FIELD_OVERHEAD;
}

impl JsonSized for SessionFilter {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + (MAX_PAGE_LIMIT * (uuid_cost() + 1)) + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + RangeFilter::JSON_SIZE + FIELD_OVERHEAD
        + RangeFilter::JSON_SIZE + FIELD_OVERHEAD
        + PaginationOptions::JSON_SIZE + FIELD_OVERHEAD;
}

impl JsonSized for SessionFilterResponse {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + ResponseMetadata::JSON_SIZE + FIELD_OVERHEAD
        + (MAX_PAGE_LIMIT * (SessionModel::JSON_SIZE + 1)) + FIELD_OVERHEAD
        + Pagination::JSON_SIZE + FIELD_OVERHEAD;
}

// Memory Size Implements
impl MemorySized for SessionModel {}
impl MemorySized for SessionFilter {}
impl MemorySized for SessionFilterResponse {}
