use crate::client::client::KrotClient;
use crate::commands::connection::{authenticate, disconnect, get_server_address, has_session, set_server_address, set_user_credentials};
use crate::security::keystore::ApplicationKeystore;
use std::sync::Arc;
use tokio::sync::Mutex;

mod client;
mod commands;
mod crypto;
mod security;

include!(concat!(env!("OUT_DIR"), "/u8_strings.rs"));

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    let keystore = Arc::new(ApplicationKeystore::new());
    let client = KrotClient::new(keystore.clone());
    let client_mutex = Arc::new(Mutex::new(client));
    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .manage(keystore)
        .manage(client_mutex)
        .invoke_handler(tauri::generate_handler![
            get_server_address,
            set_server_address,
            set_user_credentials,
            authenticate,
            disconnect,
            has_session,
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
