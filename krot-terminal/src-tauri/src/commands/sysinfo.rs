use crate::commands::error::CommandError;
use serde::Serialize;
use std::time::{Duration, SystemTime, UNIX_EPOCH};
use tauri::ipc::Channel;

#[derive(Serialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct BatteryStatus {
    pub charge: u8,
    pub is_charging: bool,
}

#[tauri::command]
pub fn get_battery_status() -> Result<BatteryStatus, CommandError> {
    Ok(fetch_battery_status())
}

#[tauri::command]
pub fn get_time() -> Result<i64, CommandError> {
    Ok(fetch_system_timestamp())
}

#[tauri::command]
pub async fn stream_battery_status(channel: Channel<BatteryStatus>) -> Result<(), CommandError> {
    tokio::spawn(async move {
        let mut status;
        loop {
            status = fetch_battery_status();
            if let Err(_) = channel.send(status) {
                break;
            }
            tokio::time::sleep(Duration::from_secs(5)).await;
        }
    });
    Ok(())
}

#[tauri::command]
pub async fn stream_time(channel: Channel<i64>) -> Result<(), CommandError> {
    tokio::spawn(async move {
        loop {
            if let Err(_) = channel.send(fetch_system_timestamp()) {
                break;
            }
            tokio::time::sleep(Duration::from_secs(1)).await;
        }
    });
    Ok(())
}

fn fetch_battery_status() -> BatteryStatus {
    if let Ok(manager) = battery::Manager::new() {
        if let Some(Ok(bat)) = manager.batteries().ok().and_then(|mut iter| iter.next()) {
            return BatteryStatus {
                charge: (bat.state_of_charge().value * 100.0) as u8,
                is_charging: matches!(bat.state(), battery::State::Charging),
            };
        }
    }
    BatteryStatus {
        charge: 100,
        is_charging: true,
    }
}

fn fetch_system_timestamp() -> i64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .map(|d| d.as_secs() as i64)
        .unwrap_or(0)
}
