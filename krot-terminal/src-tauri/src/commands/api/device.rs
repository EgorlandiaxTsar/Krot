use crate::client::api::model::common::{Pagination, PaginationOptions, RangeFilter, RequestMetadata};
use crate::client::api::model::device::{DeviceCreateRequest, DeviceDeleteCollaboratorRequest, DeviceDeleteRequest, DeviceEditRequest, DeviceFilterRequest, DeviceFilterResponse, DeviceModel, DeviceTransferOwnershipRequest, DeviceUpsertCollaboratorRequest, DEVICE_ADDRESS_BUF_LEN, DEVICE_NAME_BUF_LEN, DEVICE_PASSWORD_BUF_LEN};
use crate::client::client::KrotClient;
use crate::commands::error::CommandError;
use crate::commands::utils;
use serde::{Deserialize, Serialize};
use std::sync::Arc;
use tauri::State;

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DeviceFilterArgs {
    pub ids: Option<Vec<String>>,
    pub owner_id: Option<String>,
    pub last_updated_time: Option<RangeFilter>,
    pub creation_time: Option<RangeFilter>,
    pub name_query: Option<String>,
    pub address_query: Option<String>,
    pub pagination: PaginationOptions,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DeviceCreateArgs {
    pub name: String,
    pub password: String,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DeviceEditArgs {
    pub id: String,
    pub name: Option<String>,
    pub password: Option<String>,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DeviceUpsertCollaboratorArgs {
    pub device_id: String,
    pub user_id: String,
    pub can_read_address: Option<bool>,
    pub can_read_password: Option<bool>,
    pub can_read_last_update: Option<bool>,
    pub can_update_name: Option<bool>,
    pub can_update_password: Option<bool>,
    pub allow_programs: Option<Vec<String>>,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DeviceDeleteCollaboratorArgs {
    pub id: String,
    pub device_id: String,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DeviceTransferOwnershipArgs {
    pub id: String,
    pub new_owner_id: String,
}

#[derive(Serialize)]
pub struct DeviceFilterResult {
    pub data: Vec<DeviceModel>,
    pub pagination: Pagination,
}

#[tauri::command]
pub async fn filter_devices(args: DeviceFilterArgs, client: State<'_, Arc<KrotClient>>) -> Result<DeviceFilterResult, CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = DeviceFilterRequest {
        metadata: RequestMetadata::new(session.id),
        ids: args.ids.as_deref().map(utils::str_vec_to_uuid_vec).transpose()?,
        owner_id: args.owner_id.as_deref().map(utils::str_to_uuid).transpose()?,
        last_updated_time: args.last_updated_time,
        creation_time: args.creation_time,
        name_query: args.name_query.as_deref().map(utils::str_to_buf::<DEVICE_NAME_BUF_LEN>).transpose()?,
        address_query: args.address_query.as_deref().map(utils::str_to_buf::<DEVICE_ADDRESS_BUF_LEN>).transpose()?,
        pagination: args.pagination,
    };
    let mut out = DeviceFilterResponse::default();
    api.filter_devices(&request, &mut out).await?;
    Ok(DeviceFilterResult { data: out.data, pagination: out.pagination })
}

#[tauri::command]
pub async fn create_device(args: DeviceCreateArgs, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = DeviceCreateRequest {
        metadata: RequestMetadata::new(session.id),
        name: utils::str_to_buf::<DEVICE_NAME_BUF_LEN>(&args.name)?,
        password: utils::str_to_buf::<DEVICE_PASSWORD_BUF_LEN>(&args.password)?,
    };
    api.create_device(&request, &mut Default::default()).await?;
    Ok(())
}

#[tauri::command]
pub async fn edit_device(args: DeviceEditArgs, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = DeviceEditRequest {
        metadata: RequestMetadata::new(session.id),
        id: utils::str_to_uuid(&args.id)?,
        name: args.name.as_deref().map(utils::str_to_buf::<DEVICE_NAME_BUF_LEN>).transpose()?,
        password: args.password.as_deref().map(utils::str_to_buf::<DEVICE_PASSWORD_BUF_LEN>).transpose()?,
    };
    api.edit_device(&request, &mut Default::default()).await?;
    Ok(())
}

#[tauri::command]
pub async fn delete_devices(ids: Vec<String>, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = DeviceDeleteRequest { metadata: RequestMetadata::new(session.id), ids: utils::str_vec_to_uuid_vec(&ids)? };
    api.delete_devices(&request, &mut Default::default()).await?;
    Ok(())
}

#[tauri::command]
pub async fn upsert_device_collaborator(args: DeviceUpsertCollaboratorArgs, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = DeviceUpsertCollaboratorRequest {
        metadata: RequestMetadata::new(session.id),
        device_id: utils::str_to_uuid(&args.device_id)?,
        user_id: utils::str_to_uuid(&args.user_id)?,
        can_read_address: args.can_read_address,
        can_read_password: args.can_read_password,
        can_read_last_update: args.can_read_last_update,
        can_update_name: args.can_update_name,
        can_update_password: args.can_update_password,
        allow_programs: args.allow_programs.as_deref().map(utils::str_vec_to_uuid_vec).transpose()?,
    };
    api.upsert_device_collaborator(&request, &mut Default::default()).await?;
    Ok(())
}

#[tauri::command]
pub async fn delete_device_collaborator(args: DeviceDeleteCollaboratorArgs, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = DeviceDeleteCollaboratorRequest {
        metadata: RequestMetadata::new(session.id),
        id: utils::str_to_uuid(&args.id)?,
        device_id: utils::str_to_uuid(&args.device_id)?,
    };
    api.delete_device_collaborator(&request, &mut Default::default()).await?;
    Ok(())
}

#[tauri::command]
pub async fn transfer_device_ownership(args: DeviceTransferOwnershipArgs, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = DeviceTransferOwnershipRequest {
        metadata: RequestMetadata::new(session.id),
        id: utils::str_to_uuid(&args.id)?,
        new_owner_id: utils::str_to_uuid(&args.new_owner_id)?,
    };
    api.transfer_device_ownership(&request, &mut Default::default()).await?;
    Ok(())
}
