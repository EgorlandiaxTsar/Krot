use crate::client::api::model::common::{IdList, Pagination, PaginationOptions, RangeFilter, RequestMetadata, ResponseMetadata};
use crate::client::api::model::size::{bool_cost, i64_cost, uuid_cost, varchar_cost, JsonSized, MemorySized, BRACES_OVERHEAD, FIELD_OVERHEAD, MAX_PAGE_LIMIT};
use crate::client::types::serde::{deserialize, serialize};
use serde::{Deserialize, Serialize};

pub const PROGRAM_NAME_LEN: usize = 64;
pub const PROGRAM_NAME_BUF_LEN: usize = PROGRAM_NAME_LEN * 4;

// Models
#[repr(C)]
#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramModel {
    #[serde(deserialize_with = "deserialize::uuid")]
    pub id: [u8; 16],
    #[serde(deserialize_with = "deserialize::varchar")]
    pub name: [u8; PROGRAM_NAME_BUF_LEN],
    #[serde(deserialize_with = "deserialize::uuid")]
    pub owner_id: [u8; 16],
    pub created_at: i64,
    pub collaborators: Vec<ProgramCollaboratorModel>,
}

#[repr(C)]
#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramCollaboratorModel {
    #[serde(deserialize_with = "deserialize::uuid")]
    pub id: [u8; 16],
    #[serde(deserialize_with = "deserialize::uuid")]
    pub user_id: [u8; 16],
    #[serde(deserialize_with = "deserialize::uuid")]
    pub program_id: [u8; 16],
    pub can_update_name: bool,
    pub can_update_code: bool,
}

// Request Models
#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramFilter {
    pub metadata: RequestMetadata,
    #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::uuid_vec_opt")]
    pub ids: Option<IdList>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub creation_time: Option<RangeFilter>,
    #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::uuid_opt")]
    pub owner_id: Option<[u8; 16]>,
    #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::varchar_opt")]
    pub name_query: Option<[u8; PROGRAM_NAME_BUF_LEN]>,
    pub pagination: PaginationOptions,
}

#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramCreate {
    pub metadata: RequestMetadata,
    #[serde(serialize_with = "serialize::varchar")]
    pub name: [u8; PROGRAM_NAME_BUF_LEN],
}

#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramEdit {
    pub metadata: RequestMetadata,
    #[serde(serialize_with = "serialize::uuid")]
    pub id: [u8; 16],
    #[serde(serialize_with = "serialize::varchar")]
    pub name: [u8; PROGRAM_NAME_BUF_LEN],
}

#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramTransferOwnership {
    pub metadata: RequestMetadata,
    #[serde(serialize_with = "serialize::uuid")]
    pub id: [u8; 16],
    #[serde(serialize_with = "serialize::uuid")]
    pub new_owner_id: [u8; 16],
}

#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramDelete {
    pub metadata: RequestMetadata,
    #[serde(serialize_with = "serialize::uuid_vec")]
    pub ids: IdList,
}

#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramUpsertCollaborator {
    pub metadata: RequestMetadata,
    #[serde(serialize_with = "serialize::uuid")]
    pub program_id: [u8; 16],
    #[serde(serialize_with = "serialize::uuid")]
    pub user_id: [u8; 16],
    #[serde(skip_serializing_if = "Option::is_none")]
    pub can_update_name: Option<bool>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub can_update_code: Option<bool>,
}

#[repr(C)]
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramDeleteCollaborator {
    pub metadata: RequestMetadata,
    #[serde(serialize_with = "serialize::uuid")]
    pub id: [u8; 16],
    #[serde(serialize_with = "serialize::uuid")]
    pub program_id: [u8; 16],
}

// Response Models
#[repr(C)]
#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramFilterResponse {
    pub metadata: ResponseMetadata,
    pub data: Vec<ProgramModel>,
    pub pagination: Pagination,
}

// JSON Size Implements
impl JsonSized for ProgramCollaboratorModel {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + bool_cost() + FIELD_OVERHEAD
        + bool_cost() + FIELD_OVERHEAD;
}

impl JsonSized for ProgramModel {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + varchar_cost(PROGRAM_NAME_BUF_LEN) + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + i64_cost() + FIELD_OVERHEAD
        + (MAX_PAGE_LIMIT * (ProgramCollaboratorModel::JSON_SIZE + 1)) + FIELD_OVERHEAD;
}

impl JsonSized for ProgramFilter {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + (MAX_PAGE_LIMIT * (uuid_cost() + 1)) + FIELD_OVERHEAD
        + RangeFilter::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + varchar_cost(PROGRAM_NAME_BUF_LEN) + FIELD_OVERHEAD
        + PaginationOptions::JSON_SIZE + FIELD_OVERHEAD;
}

impl JsonSized for ProgramCreate {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + varchar_cost(PROGRAM_NAME_BUF_LEN) + FIELD_OVERHEAD;
}

impl JsonSized for ProgramUpsertCollaborator {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + 2 * (bool_cost() + FIELD_OVERHEAD);
}

impl JsonSized for ProgramDeleteCollaborator {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD;
}

impl JsonSized for ProgramEdit {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + varchar_cost(PROGRAM_NAME_BUF_LEN) + FIELD_OVERHEAD;
}

impl JsonSized for ProgramTransferOwnership {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD;
}

impl JsonSized for ProgramDelete {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + (MAX_PAGE_LIMIT * (uuid_cost() + 1)) + FIELD_OVERHEAD;
}

impl JsonSized for ProgramFilterResponse {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + ResponseMetadata::JSON_SIZE + FIELD_OVERHEAD
        + (MAX_PAGE_LIMIT * (ProgramModel::JSON_SIZE + 1)) + FIELD_OVERHEAD
        + Pagination::JSON_SIZE + FIELD_OVERHEAD;
}

// Memory Size Implements
impl MemorySized for ProgramCollaboratorModel {}
impl MemorySized for ProgramModel {}
impl MemorySized for ProgramFilter {}
impl MemorySized for ProgramCreate {}
impl MemorySized for ProgramUpsertCollaborator {}
impl MemorySized for ProgramDeleteCollaborator {}
impl MemorySized for ProgramEdit {}
impl MemorySized for ProgramTransferOwnership {}
impl MemorySized for ProgramDelete {}
impl MemorySized for ProgramFilterResponse {}
