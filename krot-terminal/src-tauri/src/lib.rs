use crate::client::client::KrotClient;
use crate::commands::api::device::{create_device, delete_device_collaborator, delete_devices, edit_device, filter_devices, transfer_device_ownership, upsert_device_collaborator};
use crate::commands::api::program::{create_program, delete_program_collaborator, delete_programs, edit_program, filter_programs, transfer_program_ownership, upsert_program_collaborator};
use crate::commands::api::role::{create_role, delete_roles, edit_role, filter_roles};
use crate::commands::api::session::filter_sessions;
use crate::commands::api::user::{create_user, delete_users, edit_user, edit_user_password, filter_users};
use crate::commands::connection::{authenticate, disconnect, get_current_user, get_server_address, has_session, set_server_address, set_user_credentials};
use crate::commands::sysinfo::{get_battery_status, get_time, stream_battery_status, stream_time};
use crate::security::keystore::ApplicationKeystore;
use std::sync::Arc;

mod client;
mod commands;
mod crypto;
mod security;

include!(concat!(env!("OUT_DIR"), "/u8_strings.rs"));

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    let client = KrotClient::new(Arc::new(ApplicationKeystore::new()));
    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .manage(Arc::new(client))
        .invoke_handler(tauri::generate_handler![
            get_server_address,
            set_server_address,
            get_current_user,
            set_user_credentials,
            authenticate,
            disconnect,
            has_session,

            filter_roles,
            create_role,
            edit_role,
            delete_roles,

            filter_users,
            create_user,
            edit_user,
            edit_user_password,
            delete_users,

            filter_devices,
            create_device,
            edit_device,
            delete_devices,
            upsert_device_collaborator,
            delete_device_collaborator,
            transfer_device_ownership,

            filter_programs,
            create_program,
            edit_program,
            delete_programs,
            upsert_program_collaborator,
            delete_program_collaborator,
            transfer_program_ownership,

            filter_sessions,

            get_battery_status,
            get_time,
            stream_battery_status,
            stream_time,
        ])
        .run(tauri::generate_context!())
        .expect("Error while running tauri application");
}
