use crate::client::api::model::common::{Pagination, PaginationOptions, RangeFilter, RequestMetadata};
use crate::client::api::model::program::PROGRAM_NAME_BUF_LEN;
use crate::client::api::model::program::{ProgramCreateRequest, ProgramDeleteCollaboratorRequest, ProgramDeleteRequest, ProgramEditRequest, ProgramFilterRequest, ProgramFilterResponse, ProgramModel, ProgramTransferOwnershipRequest, ProgramUpsertCollaboratorRequest};
use crate::client::client::KrotClient;
use crate::commands::error::CommandError;
use crate::commands::utils;
use serde::{Deserialize, Serialize};
use std::default::Default;
use std::sync::Arc;
use tauri::State;

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramFilterArgs {
    pub ids: Option<Vec<String>>,
    pub owner_id: Option<String>,
    pub creation_time: Option<RangeFilter>,
    pub name_query: Option<String>,
    pub pagination: PaginationOptions,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramCreateArgs {
    pub name: String,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramEditArgs {
    pub id: String,
    pub name: String,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramUpsertCollaboratorArgs {
    pub program_id: String,
    pub user_id: String,
    pub can_update_name: Option<bool>,
    pub can_update_code: Option<bool>,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramDeleteCollaboratorArgs {
    pub id: String,
    pub program_id: String,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ProgramTransferOwnershipArgs {
    pub id: String,
    pub new_owner_id: String,
}

#[derive(Serialize)]
pub struct ProgramFilterResult {
    pub data: Vec<ProgramModel>,
    pub pagination: Pagination,
}

#[tauri::command]
pub async fn filter_programs(args: ProgramFilterArgs, client: State<'_, Arc<KrotClient>>) -> Result<ProgramFilterResult, CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = ProgramFilterRequest {
        metadata: RequestMetadata::new(session.id),
        ids: args.ids.as_deref().map(utils::str_vec_to_uuid_vec).transpose()?,
        owner_id: args.owner_id.as_deref().map(utils::str_to_uuid).transpose()?,
        creation_time: args.creation_time,
        name_query: args.name_query.as_deref().map(utils::str_to_buf::<PROGRAM_NAME_BUF_LEN>).transpose()?,
        pagination: args.pagination,
    };
    let mut out = ProgramFilterResponse::default();
    api.filter_programs(&request, &mut out).await?;
    Ok(ProgramFilterResult { data: out.data, pagination: out.pagination })
}

#[tauri::command]
pub async fn create_program(args: ProgramCreateArgs, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = ProgramCreateRequest {
        metadata: RequestMetadata::new(session.id),
        name: utils::str_to_buf::<PROGRAM_NAME_BUF_LEN>(&args.name)?,
    };
    api.create_program(&request, &mut Default::default()).await?;
    Ok(())
}

#[tauri::command]
pub async fn edit_program(args: ProgramEditArgs, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = ProgramEditRequest {
        metadata: RequestMetadata::new(session.id),
        id: utils::str_to_uuid(&args.id)?,
        name: utils::str_to_buf::<PROGRAM_NAME_BUF_LEN>(&args.name)?,
    };
    api.edit_program(&request, &mut Default::default()).await?;
    Ok(())
}

#[tauri::command]
pub async fn delete_programs(ids: Vec<String>, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = ProgramDeleteRequest { metadata: RequestMetadata::new(session.id), ids: utils::str_vec_to_uuid_vec(&ids)? };
    api.delete_programs(&request, &mut Default::default()).await?;
    Ok(())
}

#[tauri::command]
pub async fn upsert_program_collaborator(args: ProgramUpsertCollaboratorArgs, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = ProgramUpsertCollaboratorRequest {
        metadata: RequestMetadata::new(session.id),
        program_id: utils::str_to_uuid(&args.program_id)?,
        user_id: utils::str_to_uuid(&args.user_id)?,
        can_update_name: args.can_update_name,
        can_update_code: args.can_update_code,
    };
    api.upsert_program_collaborator(&request, &mut Default::default()).await?;
    Ok(())
}

#[tauri::command]
pub async fn delete_program_collaborator(args: ProgramDeleteCollaboratorArgs, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = ProgramDeleteCollaboratorRequest {
        metadata: RequestMetadata::new(session.id),
        id: utils::str_to_uuid(&args.id)?,
        program_id: utils::str_to_uuid(&args.program_id)?,
    };
    api.delete_program_collaborator(&request, &mut Default::default()).await?;
    Ok(())
}

#[tauri::command]
pub async fn transfer_program_ownership(args: ProgramTransferOwnershipArgs, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = ProgramTransferOwnershipRequest {
        metadata: RequestMetadata::new(session.id),
        id: utils::str_to_uuid(&args.id)?,
        new_owner_id: utils::str_to_uuid(&args.new_owner_id)?,
    };
    api.transfer_program_ownership(&request, &mut Default::default()).await?;
    Ok(())
}
