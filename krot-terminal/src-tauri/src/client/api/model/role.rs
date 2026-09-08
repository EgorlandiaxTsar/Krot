use crate::client::api::model::authority::Authorities;
use crate::client::api::model::common::{IdList, Pagination, PaginationOptions, RangeFilter, RequestMetadata, ResponseMetadata};
use crate::client::api::model::size::{i32_cost, uuid_cost, varchar_cost, JsonSized, MemorySized, BRACES_OVERHEAD, FIELD_OVERHEAD, MAX_PAGE_LIMIT};
use crate::client::types::serde::{deserialize, serialize};
use serde::{Deserialize, Serialize};

pub const ROLE_NAME_LEN: usize = 32;
pub const ROLE_NAME_BUF_LEN: usize = ROLE_NAME_LEN * 4;

// Models
#[repr(C)]
#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RoleModel {
    #[serde(deserialize_with = "deserialize::uuid")]
    pub id: [u8; 16],
    #[serde(deserialize_with = "deserialize::varchar")]
    pub name: [u8; ROLE_NAME_BUF_LEN],
    pub grade: i32,
    pub authorities: Authorities,
    #[serde(deserialize_with = "deserialize::uuid_vec")]
    pub users: IdList,
}

// Request Models
#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RoleFilter {
    pub metadata: RequestMetadata,
    #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::uuid_vec_opt")]
    pub ids: Option<IdList>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub authorities: Option<Authorities>,
    #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::varchar_opt")]
    pub name_query: Option<[u8; ROLE_NAME_BUF_LEN]>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub grade: Option<RangeFilter>,
    pub pagination: PaginationOptions,
}

#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RoleCreate {
    pub metadata: RequestMetadata,
    #[serde(serialize_with = "serialize::varchar")]
    pub name: [u8; ROLE_NAME_BUF_LEN],
    pub grade: i32,
    pub authorities: Authorities,
}

#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RoleEdit {
    pub metadata: RequestMetadata,
    #[serde(serialize_with = "serialize::uuid")]
    pub id: [u8; 16],
    #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::varchar_opt")]
    pub name: Option<[u8; ROLE_NAME_BUF_LEN]>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub grade: Option<i32>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub authorities: Option<Authorities>,
}

#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RoleDelete {
    pub metadata: RequestMetadata,
    #[serde(serialize_with = "serialize::uuid_vec")]
    pub ids: IdList,
}

// Response Models
#[repr(C)]
#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RoleFilterResponse {
    pub metadata: ResponseMetadata,
    pub data: Vec<RoleModel>,
    pub pagination: Pagination,
}

// JSON Size Implements
impl JsonSized for RoleModel {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + varchar_cost(ROLE_NAME_LEN) + FIELD_OVERHEAD
        + i32_cost() + FIELD_OVERHEAD
        + Authorities::JSON_SIZE + FIELD_OVERHEAD
        + (MAX_PAGE_LIMIT * (uuid_cost() + 1)) + FIELD_OVERHEAD;
}

impl JsonSized for RoleFilter {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + (MAX_PAGE_LIMIT * (uuid_cost() + 1)) + FIELD_OVERHEAD // ids
        + Authorities::JSON_SIZE + FIELD_OVERHEAD
        + varchar_cost(ROLE_NAME_LEN) + FIELD_OVERHEAD
        + RangeFilter::JSON_SIZE + FIELD_OVERHEAD
        + PaginationOptions::JSON_SIZE + FIELD_OVERHEAD;
}

impl JsonSized for RoleCreate {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + varchar_cost(ROLE_NAME_LEN) + FIELD_OVERHEAD
        + i32_cost() + FIELD_OVERHEAD
        + Authorities::JSON_SIZE + FIELD_OVERHEAD;
}

impl JsonSized for RoleEdit {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + varchar_cost(ROLE_NAME_LEN) + FIELD_OVERHEAD
        + i32_cost() + FIELD_OVERHEAD
        + Authorities::JSON_SIZE + FIELD_OVERHEAD;
}


impl JsonSized for RoleDelete {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + (MAX_PAGE_LIMIT * (uuid_cost() + 1)) + FIELD_OVERHEAD;
}

impl JsonSized for RoleFilterResponse {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + ResponseMetadata::JSON_SIZE + FIELD_OVERHEAD
        + (MAX_PAGE_LIMIT * (RoleModel::JSON_SIZE + 1)) + FIELD_OVERHEAD
        + Pagination::JSON_SIZE + FIELD_OVERHEAD;
}

// Memory Size Implements
impl MemorySized for RoleModel {}
impl MemorySized for RoleFilter {}
impl MemorySized for RoleCreate {}
impl MemorySized for RoleEdit {}
impl MemorySized for RoleDelete {}
impl MemorySized for RoleFilterResponse {}
