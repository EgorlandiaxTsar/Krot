use crate::client::api::model::common::{Pagination, PaginationOptions, RangeFilter, RequestMetadata};
use crate::client::api::model::user::{
    UserCreateRequest, UserDeleteRequest, UserEditPasswordRequest, UserEditRequest, UserFilterRequest,
    UserFilterResponse, USER_PASSWORD_BUF_LEN, USER_USERNAME_BUF_LEN,
};
use crate::client::client::KrotClient;
use crate::commands::error::CommandError;
use crate::commands::utils;
use serde::{Deserialize, Serialize};
use std::sync::Arc;
use tauri::State;

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UserFilterArgs {
    pub ids: Option<Vec<String>>,
    pub active: Option<bool>,
    pub creation_time: Option<RangeFilter>,
    pub role_id: Option<String>,
    pub username_query: Option<String>,
    pub pagination: PaginationOptions,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UserCreateArgs {
    pub username: String,
    pub password: String,
    pub role_id: String,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UserEditArgs {
    pub id: String,
    pub username: Option<String>,
    pub role_id: Option<String>,
    pub active: Option<bool>,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UserEditPasswordArgs {
    pub id: String,
    pub password: String,
    pub new_password: String,
}

#[derive(Serialize)]
pub struct UserFilterResult {
    pub data: Vec<crate::client::api::model::user::UserModel>,
    pub pagination: Pagination,
}

#[tauri::command]
pub async fn filter_users(args: UserFilterArgs, client: State<'_, Arc<KrotClient>>) -> Result<UserFilterResult, CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = UserFilterRequest {
        metadata: RequestMetadata::new(session.id),
        ids: args.ids.as_deref().map(utils::str_vec_to_uuid_vec).transpose()?,
        active: args.active,
        creation_time: args.creation_time,
        role_id: args.role_id.as_deref().map(utils::str_to_uuid).transpose()?,
        username_query: args.username_query.as_deref().map(utils::str_to_buf::<USER_USERNAME_BUF_LEN>).transpose()?,
        pagination: args.pagination,
    };
    let mut out = UserFilterResponse::default();
    api.filter_users(&request, &mut out).await?;
    Ok(UserFilterResult { data: out.data, pagination: out.pagination })
}

#[tauri::command]
pub async fn create_user(args: UserCreateArgs, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = UserCreateRequest {
        metadata: RequestMetadata::new(session.id),
        username: utils::str_to_buf::<USER_USERNAME_BUF_LEN>(&args.username)?,
        password: utils::str_to_buf::<USER_PASSWORD_BUF_LEN>(&args.password)?,
        role_id: utils::str_to_uuid(&args.role_id)?,
    };
    api.create_user(&request, &mut Default::default()).await?;
    Ok(())
}

#[tauri::command]
pub async fn edit_user(args: UserEditArgs, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = UserEditRequest {
        metadata: RequestMetadata::new(session.id),
        id: utils::str_to_uuid(&args.id)?,
        username: args.username.as_deref().map(utils::str_to_buf::<USER_USERNAME_BUF_LEN>).transpose()?,
        role_id: args.role_id.as_deref().map(utils::str_to_uuid).transpose()?,
        active: args.active,
    };
    api.edit_user(&request, &mut Default::default()).await?;
    Ok(())
}

#[tauri::command]
pub async fn edit_user_password(args: UserEditPasswordArgs, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = UserEditPasswordRequest {
        metadata: RequestMetadata::new(session.id),
        id: utils::str_to_uuid(&args.id)?,
        password: utils::str_to_buf::<USER_PASSWORD_BUF_LEN>(&args.password)?,
        new_password: utils::str_to_buf::<USER_PASSWORD_BUF_LEN>(&args.new_password)?,
    };
    api.edit_user_password(&request, &mut Default::default()).await?;
    Ok(())
}

#[tauri::command]
pub async fn delete_users(ids: Vec<String>, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = UserDeleteRequest { metadata: RequestMetadata::new(session.id), ids: utils::str_vec_to_uuid_vec(&ids)? };
    api.delete_users(&request, &mut Default::default()).await?;
    Ok(())
}