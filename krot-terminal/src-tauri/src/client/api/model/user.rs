use crate::client::api::model::common::{IdList, Pagination, PaginationOptions, RangeFilter, RequestMetadata, ResponseMetadata};
use crate::client::api::model::size::{bool_cost, i64_cost, uuid_cost, varchar_cost, JsonSized, MemorySized, BRACES_OVERHEAD, FIELD_OVERHEAD};
use crate::client::types::serde::{deserialize, serialize};
use serde::{Deserialize, Serialize};

pub const USER_USERNAME_LEN: usize = 32; // UserEntity.username, length = 32
pub const USER_USERNAME_BUF_LEN: usize = USER_USERNAME_LEN * 4;
pub const USER_PASSWORD_LEN: usize = 32; // UserEntity.password, length = 32 (spec: minLength 8, maxLength 32)
pub const USER_PASSWORD_BUF_LEN: usize = USER_PASSWORD_LEN * 4;

// Models
#[repr(C)]
#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UserModel {
    #[serde(deserialize_with = "deserialize::uuid")]
    pub id: [u8; 16],
    #[serde(deserialize_with = "deserialize::varchar")]
    pub username: [u8; USER_USERNAME_BUF_LEN],
    #[serde(deserialize_with = "deserialize::uuid")]
    pub role_id: [u8; 16],
    pub active: bool,
    pub created_at: i64,
}

// Request Models
#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct UserFilter {
    pub metadata: RequestMetadata,
    #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::uuid_vec_opt")]
    pub ids: Option<IdList>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub active: Option<bool>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub creation_time: Option<RangeFilter>,
    #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::uuid_opt")]
    pub role_id: Option<[u8; 16]>,
    #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::varchar_opt")]
    pub username_query: Option<[u8; USER_USERNAME_BUF_LEN]>,
    pub pagination: PaginationOptions,
}

#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct UserCreate {
    pub metadata: RequestMetadata,
    #[serde(serialize_with = "serialize::varchar")]
    pub username: [u8; USER_USERNAME_BUF_LEN],
    #[serde(serialize_with = "serialize::varchar")]
    pub password: [u8; USER_PASSWORD_BUF_LEN],
    #[serde(serialize_with = "serialize::uuid")]
    pub role_id: [u8; 16],
}

#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct UserEdit {
    pub metadata: RequestMetadata,
    #[serde(serialize_with = "serialize::uuid")]
    pub id: [u8; 16],
    #[serde(serialize_with = "serialize::varchar_opt")]
    pub username: Option<[u8; USER_USERNAME_BUF_LEN]>,
    #[serde(serialize_with = "serialize::uuid_opt")]
    pub role_id: Option<[u8; 16]>,
    pub active: Option<bool>,
}

#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct UserEditPassword {
    pub metadata: RequestMetadata,
    #[serde(serialize_with = "serialize::uuid")]
    pub id: [u8; 16],
    #[serde(serialize_with = "serialize::varchar")]
    pub password: [u8; USER_PASSWORD_BUF_LEN],
    #[serde(serialize_with = "serialize::varchar")]
    pub new_password: [u8; USER_PASSWORD_BUF_LEN],
}

#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct UserDelete {
    pub metadata: RequestMetadata,
    #[serde(serialize_with = "serialize::uuid_vec")]
    pub ids: IdList,
}

// Response Models
#[repr(C)]
#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UserFilterResponse {
    pub metadata: ResponseMetadata,
    pub data: Vec<UserModel>,
    pub pagination: Pagination,
}

// JSON Size Implements
impl JsonSized for UserModel {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + varchar_cost(USER_USERNAME_BUF_LEN) + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + bool_cost() + FIELD_OVERHEAD
        + i64_cost() + FIELD_OVERHEAD;
}

impl JsonSized for UserFilter {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + (crate::client::api::model::size::MAX_PAGE_LIMIT * (uuid_cost() + 1)) + FIELD_OVERHEAD // ids
        + bool_cost() + FIELD_OVERHEAD
        + RangeFilter::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + varchar_cost(USER_USERNAME_BUF_LEN) + FIELD_OVERHEAD
        + PaginationOptions::JSON_SIZE + FIELD_OVERHEAD;
}

impl JsonSized for UserCreate {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + varchar_cost(USER_USERNAME_BUF_LEN) + FIELD_OVERHEAD
        + varchar_cost(USER_PASSWORD_BUF_LEN) + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD;
}

impl JsonSized for UserEdit {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + varchar_cost(USER_USERNAME_BUF_LEN) + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + bool_cost() + FIELD_OVERHEAD;
}

impl JsonSized for UserEditPassword {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + varchar_cost(USER_PASSWORD_BUF_LEN) + FIELD_OVERHEAD
        + varchar_cost(USER_PASSWORD_BUF_LEN) + FIELD_OVERHEAD;
}

impl JsonSized for UserDelete {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + (crate::client::api::model::size::MAX_PAGE_LIMIT * (uuid_cost() + 1)) + FIELD_OVERHEAD;
}

impl JsonSized for UserFilterResponse {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + ResponseMetadata::JSON_SIZE + FIELD_OVERHEAD
        + (crate::client::api::model::size::MAX_PAGE_LIMIT * (UserModel::JSON_SIZE + 1)) + FIELD_OVERHEAD
        + Pagination::JSON_SIZE + FIELD_OVERHEAD;
}

// Memory Size Implements
impl MemorySized for UserModel {}
impl MemorySized for UserFilter {}
impl MemorySized for UserCreate {}
impl MemorySized for UserEdit {}
impl MemorySized for UserEditPassword {}
impl MemorySized for UserDelete {}
impl MemorySized for UserFilterResponse {}