use crate::client::api::model::authority::{Authorities, Authority};
use crate::client::types::serde::deserialize::parse_uuid;
use crate::commands::error::CommandError;

pub fn validate_length(s: &str, min: usize, max: usize) -> Result<(), CommandError> {
    if s.len() < min || s.len() > max {
        return Err(CommandError::Argument(format!("length must be between {min} and {max}, got {}", s.len())));
    }
    Ok(())
}

pub fn str_to_uuid(s: &str) -> Result<[u8; 16], CommandError> {
    parse_uuid(s.as_bytes()).map_err(|e| CommandError::Argument(e.to_string()))
}

pub fn str_vec_to_uuid_vec(items: &[String]) -> Result<Vec<[u8; 16]>, CommandError> {
    items.iter().map(|s| str_to_uuid(s)).collect()
}

pub fn str_to_buf<const N: usize>(s: &str) -> Result<[u8; N], CommandError> {
    let bytes = s.as_bytes();
    if bytes.len() > N {
        return Err(CommandError::Argument(format!("Value too long: {} bytes, max {}", bytes.len(), N)));
    }
    let mut buf = [0u8; N];
    buf[..bytes.len()].copy_from_slice(bytes);
    Ok(buf)
}

pub fn str_to_authorities(names: &[String]) -> Result<Authorities, CommandError> {
    let mut out = Authorities::default();
    for n in names {
        out.insert(Authority::from_str(n).ok_or_else(|| CommandError::Argument(format!("Unknown authority: {n}")))?);
    }
    Ok(out)
}