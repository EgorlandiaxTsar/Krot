use std::io::Write;
use std::env;
use std::fs::File;
use std::path::Path;

fn main() {
    tauri_build::build();
    create_byte_str_array();
}

fn create_byte_str_array() {
    let dest = Path::new(&env::var("OUT_DIR").unwrap()).join("u8_strings.rs");
    let mut file = File::create(&dest).unwrap();
    writeln!(file, "pub const U8_STRINGS: [&str; 256] = [").unwrap();
    for i in 0..256 { write!(file, "\t\"{}\",", i).unwrap(); }
    writeln!(file, "];").unwrap();
    println!("cargo:rerun-if-changed=build.rs");
}
