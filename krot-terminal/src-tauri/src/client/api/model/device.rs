use crate::client::api::model::common::{IdList, Pagination, PaginationOptions, RangeFilter, RequestMetadata, ResponseMetadata};
use crate::client::api::model::size::{bool_cost, i64_cost, uuid_cost, varchar_cost, JsonSized, MemorySized, BRACES_OVERHEAD, FIELD_OVERHEAD, MAX_PAGE_LIMIT};
use crate::client::types::serde::{deserialize, serialize};
use crate::client::utils::new_model;

pub const DEVICE_NAME_LEN: usize = 128; // DeviceEntity.name, length = 128
pub const DEVICE_NAME_BUF_LEN: usize = DEVICE_NAME_LEN * 4;
pub const DEVICE_ADDRESS_LEN: usize = 16; // DeviceEntity.address, length = 16
pub const DEVICE_ADDRESS_BUF_LEN: usize = DEVICE_ADDRESS_LEN * 4;
pub const DEVICE_PASSWORD_LEN: usize = 32; // DeviceEntity.password, length = 32
pub const DEVICE_PASSWORD_BUF_LEN: usize = DEVICE_PASSWORD_LEN * 4;

// Models
new_model! {
    pub struct DeviceModel {
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub id: [u8; 16],
        #[serde(serialize_with = "serialize::varchar", deserialize_with = "deserialize::varchar")]
        pub name: [u8; DEVICE_NAME_BUF_LEN],
        #[serde(serialize_with = "serialize::varchar", deserialize_with = "deserialize::varchar")]
        pub address: [u8; DEVICE_ADDRESS_BUF_LEN],
        #[serde(serialize_with = "serialize::varchar", deserialize_with = "deserialize::varchar")]
        pub password: [u8; DEVICE_PASSWORD_BUF_LEN],
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub owner_id: [u8; 16],
        pub last_called: i64,
        pub created_at: i64,
        pub collaborators: Vec<DeviceCollaboratorModel>,
    }
}

new_model! {
    pub struct DeviceCollaboratorModel {
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub id: [u8; 16],
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub user_id: [u8; 16],
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub device_id: [u8; 16],
        pub can_read_address: bool,
        pub can_read_password: bool,
        pub can_read_last_update: bool,
        pub can_update_name: bool,
        pub can_update_password: bool,
        #[serde(serialize_with = "serialize::uuid_vec", deserialize_with = "deserialize::uuid_vec")]
        pub allow_programs: IdList,
    }
}

// Request Models
new_model! {
    pub struct DeviceFilterRequest {
        pub metadata: RequestMetadata,
        #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::uuid_vec_opt", deserialize_with = "deserialize::uuid_vec_opt")]
        pub ids: Option<IdList>,
        #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::uuid_opt", deserialize_with = "deserialize::uuid_opt")]
        pub owner_id: Option<[u8; 16]>,
        #[serde(skip_serializing_if = "Option::is_none")]
        pub last_updated_time: Option<RangeFilter>,
        #[serde(skip_serializing_if = "Option::is_none")]
        pub creation_time: Option<RangeFilter>,
        #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::varchar_opt", deserialize_with = "deserialize::varchar_opt")]
        pub name_query: Option<[u8; DEVICE_NAME_BUF_LEN]>,
        #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::varchar_opt", deserialize_with = "deserialize::varchar_opt")]
        pub address_query: Option<[u8; DEVICE_ADDRESS_BUF_LEN]>,
        pub pagination: PaginationOptions,
    }
}

new_model! {
    pub struct DeviceCreateRequest {
        pub metadata: RequestMetadata,
        #[serde(serialize_with = "serialize::varchar", deserialize_with = "deserialize::varchar")]
        pub name: [u8; DEVICE_NAME_BUF_LEN],
        #[serde(serialize_with = "serialize::varchar", deserialize_with = "deserialize::varchar")]
        pub password: [u8; DEVICE_PASSWORD_BUF_LEN],
    }
}

new_model! {
    pub struct DeviceEditRequest {
        pub metadata: RequestMetadata,
        #[serde(serialize_with = "serialize::uuid")]
        pub id: [u8; 16],
        #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::varchar_opt", deserialize_with = "deserialize::varchar_opt")]
        pub name: Option<[u8; DEVICE_NAME_BUF_LEN]>,
        #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::varchar_opt", deserialize_with = "deserialize::varchar_opt")]
        pub password: Option<[u8; DEVICE_PASSWORD_BUF_LEN]>,
    }
}

new_model! {
    pub struct DeviceDeleteRequest {
        pub metadata: RequestMetadata,
        #[serde(serialize_with = "serialize::uuid_vec", deserialize_with = "deserialize::uuid_vec")]
        pub ids: IdList,
    }
}

new_model! {
    pub struct DeviceUpsertCollaboratorRequest {
        pub metadata: RequestMetadata,
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub device_id: [u8; 16],
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub user_id: [u8; 16],
        #[serde(skip_serializing_if = "Option::is_none")]
        pub can_read_address: Option<bool>,
        #[serde(skip_serializing_if = "Option::is_none")]
        pub can_read_password: Option<bool>,
        #[serde(skip_serializing_if = "Option::is_none")]
        pub can_read_last_update: Option<bool>,
        #[serde(skip_serializing_if = "Option::is_none")]
        pub can_update_name: Option<bool>,
        #[serde(skip_serializing_if = "Option::is_none")]
        pub can_update_password: Option<bool>,
        #[serde(skip_serializing_if = "Option::is_none", serialize_with = "serialize::uuid_vec_opt", deserialize_with = "deserialize::uuid_vec_opt")]
        pub allow_programs: Option<IdList>,
    }
}

new_model! {
    pub struct DeviceDeleteCollaboratorRequest {
        pub metadata: RequestMetadata,
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub id: [u8; 16],
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub device_id: [u8; 16],
    }
}

new_model! {
    pub struct DeviceTransferOwnershipRequest {
        pub metadata: RequestMetadata,
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub id: [u8; 16],
        #[serde(serialize_with = "serialize::uuid", deserialize_with = "deserialize::uuid")]
        pub new_owner_id: [u8; 16],
    }
}

// Response Models
new_model! {
    pub struct DeviceFilterResponse {
        pub metadata: ResponseMetadata,
        pub data: Vec<DeviceModel>,
        pub pagination: Pagination,
    }
}

// JSON Size Implements
impl JsonSized for DeviceCollaboratorModel {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD    // id
        + uuid_cost() + FIELD_OVERHEAD    // userId
        + uuid_cost() + FIELD_OVERHEAD    // deviceId
        + bool_cost() + FIELD_OVERHEAD    // canReadAddress
        + bool_cost() + FIELD_OVERHEAD    // canReadPassword
        + bool_cost() + FIELD_OVERHEAD    // canReadLastUpdate
        + bool_cost() + FIELD_OVERHEAD    // canUpdateName
        + bool_cost() + FIELD_OVERHEAD    // canUpdatePassword
        + (MAX_PAGE_LIMIT * (uuid_cost() + 1)) + FIELD_OVERHEAD; // allowPrograms
}

impl JsonSized for DeviceModel {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + varchar_cost(DEVICE_NAME_BUF_LEN) + FIELD_OVERHEAD
        + varchar_cost(DEVICE_ADDRESS_BUF_LEN) + FIELD_OVERHEAD
        + varchar_cost(DEVICE_PASSWORD_BUF_LEN) + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + i64_cost() + FIELD_OVERHEAD
        + i64_cost() + FIELD_OVERHEAD
        + (MAX_PAGE_LIMIT * (DeviceCollaboratorModel::JSON_SIZE + 1)) + FIELD_OVERHEAD;
}

impl JsonSized for DeviceFilterRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + (MAX_PAGE_LIMIT * (uuid_cost() + 1)) + FIELD_OVERHEAD // ids
        + uuid_cost() + FIELD_OVERHEAD                          // ownerId
        + RangeFilter::JSON_SIZE + FIELD_OVERHEAD               // lastUpdatedTime
        + RangeFilter::JSON_SIZE + FIELD_OVERHEAD               // creationTime
        + varchar_cost(DEVICE_NAME_BUF_LEN) + FIELD_OVERHEAD
        + varchar_cost(DEVICE_ADDRESS_BUF_LEN) + FIELD_OVERHEAD
        + PaginationOptions::JSON_SIZE + FIELD_OVERHEAD;
}

impl JsonSized for DeviceCreateRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + varchar_cost(DEVICE_NAME_BUF_LEN) + FIELD_OVERHEAD
        + varchar_cost(DEVICE_PASSWORD_BUF_LEN) + FIELD_OVERHEAD;
}

impl JsonSized for DeviceEditRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + varchar_cost(DEVICE_NAME_BUF_LEN) + FIELD_OVERHEAD
        + varchar_cost(DEVICE_PASSWORD_BUF_LEN) + FIELD_OVERHEAD;
}

impl JsonSized for DeviceUpsertCollaboratorRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + 5 * (bool_cost() + FIELD_OVERHEAD)
        + (MAX_PAGE_LIMIT * (uuid_cost() + 1)) + FIELD_OVERHEAD;
}

impl JsonSized for DeviceDeleteCollaboratorRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD;
}

impl JsonSized for DeviceDeleteRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + (MAX_PAGE_LIMIT * (uuid_cost() + 1)) + FIELD_OVERHEAD;
}

impl JsonSized for DeviceTransferOwnershipRequest {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + RequestMetadata::JSON_SIZE + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD
        + uuid_cost() + FIELD_OVERHEAD;
}

impl JsonSized for DeviceFilterResponse {
    const JSON_SIZE: usize = BRACES_OVERHEAD
        + ResponseMetadata::JSON_SIZE + FIELD_OVERHEAD
        + (MAX_PAGE_LIMIT * (DeviceModel::JSON_SIZE + 1)) + FIELD_OVERHEAD
        + Pagination::JSON_SIZE + FIELD_OVERHEAD;
}

// Memory Size Implements
impl MemorySized for DeviceCollaboratorModel {}
impl MemorySized for DeviceModel {}
impl MemorySized for DeviceFilterRequest {}
impl MemorySized for DeviceCreateRequest {}
impl MemorySized for DeviceEditRequest {}
impl MemorySized for DeviceUpsertCollaboratorRequest {}
impl MemorySized for DeviceDeleteCollaboratorRequest {}
impl MemorySized for DeviceDeleteRequest {}
impl MemorySized for DeviceTransferOwnershipRequest {}
impl MemorySized for DeviceFilterResponse {}