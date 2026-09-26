use crate::client::client::KrotClient;
use crate::commands::error::CommandError;
use crate::commands::utils;
use serde::{Deserialize, Serialize};
use std::net::Ipv4Addr;
use std::str::FromStr;
use std::sync::Arc;
use tauri::State;

#[derive(Serialize, Deserialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct ServerAddress {
    pub ip: String,
    pub port: u16,
    pub secured: bool,
}

#[derive(Deserialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct UserCredentials {
    pub username: String,
    pub password: String,
}

#[tauri::command]
pub async fn get_server_address(client: State<'_, Arc<KrotClient>>) -> Result<ServerAddress, CommandError> {
    let credentials = client.credentials()?;
    Ok(ServerAddress {
        ip: format!("{}.{}.{}.{}", credentials.addr[0], credentials.addr[1], credentials.addr[2], credentials.addr[3]),
        port: credentials.port,
        secured: credentials.secured,
    })
}

#[tauri::command]
pub async fn set_server_address(args: ServerAddress, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    let ip = Ipv4Addr::from_str(&args.ip).map_err(|_| CommandError::Argument("bad IPv4 address".into()))?;
    client.update_server_address(ip.octets(), args.port, args.secured).await.map_err(|_| CommandError::Hello)?;
    Ok(())
}

#[tauri::command]
pub async fn get_current_user(client: State<'_, Arc<KrotClient>>) -> Result<String, CommandError> {
    let credentials = client.credentials()?;
    let end = credentials.name.iter().position(|&b| b == 0).unwrap_or(credentials.name.len());
    std::str::from_utf8(&credentials.name[..end])
        .map(|s| s.to_string())
        .map_err(|_| CommandError::Internal("stored username is not valid UTF-8".into()))
}

#[tauri::command]
pub async fn set_user_credentials(args: UserCredentials, client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    utils::validate_length(&args.username, 1, 32)?;
    utils::validate_length(&args.password, 1, 32)?;
    client.update_credentials(&args.username, &args.password).await.map_err(CommandError::Session)?;
    Ok(())
}

#[tauri::command]
pub async fn authenticate(client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    client.authenticate().await.map_err(CommandError::Authentication)
}

#[tauri::command]
pub async fn disconnect(client: State<'_, Arc<KrotClient>>) -> Result<(), CommandError> {
    client.disconnect().await.map_err(CommandError::Session)
}

#[tauri::command]
pub async fn has_session(client: State<'_, Arc<KrotClient>>) -> Result<bool, CommandError> {
    Ok(client.authenticated())
}