use crate::client::client::KrotClient;
use crate::client::confidential::Credentials;
use crate::client::error::ClientError;
use crate::commands::error::CommandError;
use crate::security::error::SecurityError;
use crate::security::keystore::{ApplicationKeystore, CredentialsKeystore, Keystore};
use serde::{Deserialize, Serialize};
use std::net::Ipv4Addr;
use std::str::FromStr;
use std::sync::Arc;
use tauri::State;
use tokio::sync::{Mutex, MutexGuard};

#[derive(Serialize, Deserialize)]
pub struct ServerAddress {
    pub ip: String,
    pub port: u16,
    pub secured: bool,
}

#[derive(Deserialize)]
pub struct UserCredentials {
    pub username: String,
    pub password: String,
}

#[tauri::command]
pub async fn get_server_address(keystore: State<'_, Arc<ApplicationKeystore>>) -> Result<ServerAddress, CommandError> {
    let mut credentials = Credentials::default();
    keystore.credentials_keystore.read(CredentialsKeystore::IDX, &mut credentials).map_err(|_| CommandError::CredentialsNotFound)?;
    Ok(ServerAddress {
        ip: format!("{}.{}.{}.{}", credentials.addr[0], credentials.addr[1], credentials.addr[2], credentials.addr[3]),
        port: credentials.port,
        secured: credentials.secured,
    })
}

#[tauri::command]
pub async fn set_server_address(
    args: ServerAddress,
    keystore: State<'_, Arc<ApplicationKeystore>>,
    client: State<'_, Arc<Mutex<KrotClient>>>,
) -> Result<(), CommandError> {
    let ip = Ipv4Addr::from_str(&args.ip).map_err(|_| CommandError::ArgumentError("Bad IPv4 address".to_string()))?;
    let mut credentials = Credentials::default();
    fetch_credentials(&keystore, &mut credentials)?;
    credentials.addr = ip.octets();
    credentials.port = args.port;
    credentials.secured = args.secured;
    let mut client = client.lock().await;
    KrotClient::hello(&client.http, &credentials.addr, &credentials.port, credentials.secured).await.map_err(|e| CommandError::ClientHelloFailed(e))?;
    disconnect_client(&client).await?;
    keystore.credentials_keystore.store(CredentialsKeystore::IDX, &mut credentials).map_err(|e| CommandError::KeystoreAccessFailed(e))?;
    client.refresh_credentials().map_err(|e| CommandError::ClientCredentialsUpdateFailed(e))?;
    Ok(())
}

#[tauri::command]
pub async fn set_user_credentials(
    args: UserCredentials,
    keystore: State<'_, Arc<ApplicationKeystore>>,
    client: State<'_, Arc<Mutex<KrotClient>>>,
) -> Result<(), CommandError> {
    validate_length(&args.username, 1, 32)?;
    validate_length(&args.password, 1, 32)?;
    let mut credentials = Credentials::default();
    fetch_credentials(&keystore, &mut credentials)?;
    credentials.name[..args.username.as_bytes().len()].copy_from_slice(args.username.as_bytes());
    credentials.pwd[..args.password.as_bytes().len()].copy_from_slice(args.password.as_bytes());
    keystore.credentials_keystore.store(CredentialsKeystore::IDX, &mut credentials).map_err(|e| CommandError::KeystoreAccessFailed(e))?;
    let mut client = client.lock().await;
    disconnect_client(&client).await?;
    client.refresh_credentials().map_err(|e| CommandError::ClientCredentialsUpdateFailed(e))?;
    client.refresh_session().await.map_err(|e| CommandError::ClientAuthenticationFailed(e))?;
    Ok(())
}

#[tauri::command]
pub async fn authenticate(client: State<'_, Arc<Mutex<KrotClient>>>) -> Result<(), CommandError> {
    let client = client.lock().await;
    client.refresh_session().await.map_err(|e| CommandError::ClientAuthenticationFailed(e))?;
    Ok(())
}

#[tauri::command]
pub async fn disconnect(client: State<'_, Arc<Mutex<KrotClient>>>) -> Result<(), CommandError> {
    let client = client.lock().await;
    client.disconnect().await.map_err(|e| CommandError::ClientDisconnectFailed(e))?;
    Ok(())
}

#[tauri::command]
pub async fn has_session(client: State<'_, Arc<Mutex<KrotClient>>>) -> Result<bool, CommandError> {
    match client.lock().await.chk_session() {
        Ok(_) => { Ok(true) }
        Err(_) => { Ok(false) }
    }
}

async fn disconnect_client(client: &MutexGuard<'_, KrotClient>) -> Result<(), CommandError> {
    match client.disconnect().await {
        Ok(_) => Ok(()),
        Err(ClientError::SessionExpired) => Ok(()),
        Err(ClientError::SessionNotFound) => Ok(()),
        Err(e) => Err(CommandError::ClientDisconnectFailed(e)),
    }
}

fn fetch_credentials(keystore: &Arc<ApplicationKeystore>, out: &mut Credentials) -> Result<(), CommandError> {
    match keystore.credentials_keystore.read(CredentialsKeystore::IDX, out) {
        Ok(_) => {}
        Err(SecurityError::NotFound) => {}
        Err(e) => return Err(CommandError::KeystoreAccessFailed(e))
    }
    Ok(())
}

fn validate_length(s: &str, min: usize, max: usize) -> Result<(), CommandError> {
    if s.len() < min || s.len() > max {
        return Err(CommandError::ArgumentError(format!("{} has wrong length, required between 1 and 32, given {}", s, s.len())));
    }
    Ok(())
}
