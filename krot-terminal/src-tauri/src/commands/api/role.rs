use crate::client::api::model::common::{Pagination, PaginationOptions, RangeFilter, RequestMetadata};
use crate::client::api::model::role::{RoleCreateRequest, RoleDeleteRequest, RoleEditRequest, RoleFilterRequest, RoleFilterResponse};
use crate::client::client::KrotClient;
use crate::commands::error::CommandError;
use crate::commands::utils;
use serde::{Deserialize, Serialize};
use std::sync::Arc;
use tauri::State;

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RoleFilterArgs {
    pub ids: Option<Vec<String>>,
    pub authorities: Option<Vec<String>>,
    pub name_query: Option<String>,
    pub grade: Option<RangeFilter>,
    pub pagination: PaginationOptions,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RoleCreateArgs {
    pub name: String,
    pub grade: i32,
    pub authorities: Vec<String>,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RoleEditArgs {
    pub id: String,
    pub name: Option<String>,
    pub grade: Option<i32>,
    pub authorities: Option<Vec<String>>,
}

#[derive(Serialize)]
pub struct RoleFilterResult {
    pub data: Vec<crate::client::api::model::role::RoleModel>,
    pub pagination: Pagination,
}

#[tauri::command]
pub async fn filter_roles(args: RoleFilterArgs, client: State<'_, Arc<KrotClient>>) -> Result<RoleFilterResult, CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = RoleFilterRequest {
        metadata: RequestMetadata::new(session.id),
        ids: args.ids.as_deref().map(utils::str_vec_to_uuid_vec).transpose()?,
        authorities: args.authorities.as_deref().map(utils::str_to_authorities).transpose()?,
        name_query: args.name_query.as_deref().map(utils::str_to_buf).transpose()?,
        grade: args.grade,
        pagination: args.pagination,
    };
    let mut out = RoleFilterResponse::default();
    api.filter_roles(&request, &mut out).await?;
    Ok(RoleFilterResult { data: out.data, pagination: out.pagination })
}

#[tauri::command]
pub async fn create_role(args: RoleCreateArgs, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = RoleCreateRequest {
        metadata: RequestMetadata::new(session.id),
        name: utils::str_to_buf(&args.name)?,
        grade: args.grade,
        authorities: utils::str_to_authorities(&args.authorities)?,
    };
    api.create_role(&request, &mut Default::default()).await?;
    Ok(())
}

#[tauri::command]
pub async fn edit_role(args: RoleEditArgs, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = RoleEditRequest {
        metadata: RequestMetadata::new(session.id),
        id: utils::str_to_uuid(&args.id)?,
        name: args.name.as_deref().map(utils::str_to_buf).transpose()?,
        grade: args.grade,
        authorities: args.authorities.as_deref().map(utils::str_to_authorities).transpose()?,
    };
    api.edit_role(&request, &mut Default::default()).await?;
    Ok(())
}

#[tauri::command]
pub async fn delete_roles(ids: Vec<String>, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = RoleDeleteRequest { metadata: RequestMetadata::new(session.id), ids: utils::str_vec_to_uuid_vec(&ids)? };
    api.delete_roles(&request, &mut Default::default()).await?;
    Ok(())
}