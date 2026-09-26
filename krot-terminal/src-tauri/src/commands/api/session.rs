use crate::client::api::model::common::{Pagination, PaginationOptions, RangeFilter, RequestMetadata};
use crate::client::api::model::session::{SessionFilterRequest, SessionFilterResponse};
use crate::client::client::KrotClient;
use crate::commands::error::CommandError;
use crate::commands::utils;
use serde::{Deserialize, Serialize};
use std::sync::Arc;
use tauri::State;

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SessionFilterArgs {
    pub ids: Option<Vec<String>>,
    pub owner_id: Option<String>,
    pub valid_until_time: Option<RangeFilter>,
    pub creation_time: Option<RangeFilter>,
    pub pagination: PaginationOptions,
}

#[derive(Serialize)]
pub struct SessionFilterResult {
    pub data: Vec<crate::client::api::model::session::SessionModel>,
    pub pagination: Pagination,
}

#[tauri::command]
pub async fn filter_sessions(args: SessionFilterArgs, client: State<'_, Arc<KrotClient>>) -> Result<SessionFilterResult, CommandError> {
    let api = client.api().await.map_err(CommandError::Session)?;
    let session = client.session().map_err(CommandError::Session)?;
    let request = SessionFilterRequest {
        metadata: RequestMetadata::new(session.id),
        ids: args.ids.as_deref().map(utils::str_vec_to_uuid_vec).transpose()?,
        owner_id: args.owner_id.as_deref().map(utils::str_to_uuid).transpose()?,
        valid_until_time: args.valid_until_time,
        creation_time: args.creation_time,
        pagination: args.pagination,
    };
    let mut out = SessionFilterResponse::default();
    api.filter_sessions(&request, &mut out).await?;
    Ok(SessionFilterResult { data: out.data, pagination: out.pagination })
}
