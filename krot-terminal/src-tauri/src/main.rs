// Prevents additional console window on Windows in release, DO NOT REMOVE!!
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

mod client;
mod commands;
mod crypto;
mod security;

include!(concat!(env!("OUT_DIR"), "/u8_strings.rs"));

fn main() {
    krot_terminal_lib::run()
}
