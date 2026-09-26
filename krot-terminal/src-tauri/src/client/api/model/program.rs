use crate::client::api::model::common::{IdList, Pagination, PaginationOptions, RangeFilter, RequestMetadata, ResponseMetadata};
use crate::client::api::model::size::{bool_cost, i64_cost, uuid_cost, varchar_cost, JsonSized, MemorySized, BRACES_OVERHEAD, FIELD_OVERHEAD, MAX_PAGE_LIMIT};
use crate::client::types::serde::{deserialize, serialize};
use crate::client::utils::new_model;

pub const PROGRAM_NAME_LEN: usize = 64;
pub const PROGRAM_NAME_BUF_LEN: usize = PROGRAM_NAME_LEN * 4;

// Models
new_model! {
    pub struct ProgramModel {
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub id: [u8; 16],
        #[serde(serialize_with = "serialize::varchar", deserialize_with = "deserialize::varchar")]
        pub name: [u8; PROGRAM_NAME_BUF_LEN],
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub owner_id: [u8; 16],
        pub created_at: i64,
        pub collaborators: Vec<ProgramCollaboratorModel>,
    }
}

new_model! {
    pub struct ProgramCollaboratorModel {
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub id: [u8; 16],
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub user_id: [u8; 16],
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub program_id: [u8; 16],
        pub can_update_name: bool,
        pub can_update_code: bool,
    }
}

// Request Models
new_model! {
    pub struct ProgramFilterRequest {
        pub metadata: RequestMetadata,
        #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::uuid_vec_opt", deserialize_with = "deserialize::uuid_vec_opt")]
        pub ids: Option<IdList>,
        #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::uuid_opt", deserialize_with = "deserialize::uuid_opt")]
        pub owner_id: Option<[u8; 16]>,
        #[serde(skip_serializing_if = "Option::is_none")]
        pub creation_time: Option<RangeFilter>,
        #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::varchar_opt", deserialize_with = "deserialize::varchar_opt")]
        pub name_query: Option<[u8; PROGRAM_NAME_BUF_LEN]>,
        pub pagination: PaginationOptions,
    }
}

new_model! {
    pub struct ProgramCreateRequest {
        pub metadata: RequestMetadata,
        #[serde(serialize_with = "serialize::varchar", deserialize_with = "deserialize::varchar")]
        pub name: [u8; PROGRAM_NAME_BUF_LEN],
    }
}

new_model! {
    pub struct ProgramEditRequest {
        pub metadata: RequestMetadata,
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub id: [u8; 16],
        #[serde(serialize_with = "serialize::varchar", deserialize_with = "deserialize::varchar")]
        pub name: [u8; PROGRAM_NAME_BUF_LEN],
    }
}

new_model! {
    pub struct ProgramDeleteRequest {
        pub metadata: RequestMetadata,
        #[serde(serialize_with = "serialize::uuid_vec", deserialize_with = "deserialize::uuid_vec")]
        pub ids: IdList,
    }
}

new_model! {
    pub struct ProgramUpsertCollaboratorRequest {
        pub metadata: RequestMetadata,
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub program_id: [u8; 16],
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub user_id: [u8; 16],
        #[serde(skip_serializing_if = "Option::is_none")]
        pub can_update_name: Option<bool>,
        #[serde(skip_serializing_if = "Option::is_none")]
        pub can_update_code: Option<bool>,
    }
}

new_model! {
    pub struct ProgramDeleteCollaboratorRequest {
        pub metadata: RequestMetadata,
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub id: [u8; 16],
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub program_id: [u8; 16],
    }
}

new_model! {
    pub struct ProgramTransferOwnershipRequest {
        pub metadata: RequestMetadata,
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub id: [u8; 16],
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub new_owner_id: [u8; 16],
    }
}

// Response Models
new_model! {
    pub struct ProgramFilterResponse {
        pub metadata: ResponseMetadata,
        pub data: Vec<ProgramModel>,
        pub pagination: Pagination,
    }
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

impl JsonSized for ProgramFilterRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + (MAX_PAGE_LIMIT * (uuid_cost() + 1)) + FIELD_OVERHEAD
        + RangeFilter::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + varchar_cost(PROGRAM_NAME_BUF_LEN) + FIELD_OVERHEAD
        + PaginationOptions::JSON_SIZE + FIELD_OVERHEAD;
}

impl JsonSized for ProgramCreateRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + varchar_cost(PROGRAM_NAME_BUF_LEN) + FIELD_OVERHEAD;
}

impl JsonSized for ProgramUpsertCollaboratorRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + 2 * (bool_cost() + FIELD_OVERHEAD);
}

impl JsonSized for ProgramDeleteCollaboratorRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD;
}

impl JsonSized for ProgramEditRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + varchar_cost(PROGRAM_NAME_BUF_LEN) + FIELD_OVERHEAD;
}

impl JsonSized for ProgramTransferOwnershipRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD;
}

impl JsonSized for ProgramDeleteRequest {
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
impl MemorySized for ProgramFilterRequest {}
impl MemorySized for ProgramCreateRequest {}
impl MemorySized for ProgramUpsertCollaboratorRequest {}
impl MemorySized for ProgramDeleteCollaboratorRequest {}
impl MemorySized for ProgramEditRequest {}
impl MemorySized for ProgramTransferOwnershipRequest {}
impl MemorySized for ProgramDeleteRequest {}
impl MemorySized for ProgramFilterResponse {}
