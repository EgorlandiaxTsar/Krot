use crate::client::client::{IpBuffer, PathBuffer, PATH_BUFFER_LEN};
use crate::client::error::ClientError;
use std::io::Write;
use std::time;

pub fn timestamp() -> Result<i64, ClientError> {
    Ok(time::SystemTime::now()
        .duration_since(time::UNIX_EPOCH)
        .map_err(|_| ClientError::TimestampError)?
        .as_millis() as i64)
}

pub fn new_url(bytes: &[u8]) -> PathBuffer {
    let mut buf = [0u8; 256];
    buf[..bytes.len()].copy_from_slice(bytes);
    buf
}

pub fn new_custom_scheme_url(
    ip: &IpBuffer,
    port: &u16,
    path: &PathBuffer,
    secured: bool,
    unsecured_scheme: &'static str,
    secured_scheme: &'static str,
    out: &mut [u8],
) -> Result<(), ClientError> {
    let mut cursor = out;
    let scheme = if secured { secured_scheme } else { unsecured_scheme };
    let p_len = path.iter().position(|&byte| byte == 0).unwrap_or(PATH_BUFFER_LEN);
    let p_str = str::from_utf8(&path[..p_len]).map_err(|_| ClientError::UrlError)?;
    write!(
        &mut cursor,
        "{}://{}.{}.{}.{}:{}{}",
        scheme, ip[0], ip[1], ip[2], ip[3], port, p_str
    ).map_err(|_| ClientError::UrlError)?;
    Ok(())
}

pub fn new_http_url(
    ip: &IpBuffer,
    port: &u16,
    path: &PathBuffer,
    secured: bool,
    out: &mut [u8],
) -> Result<(), ClientError> {
    new_custom_scheme_url(
        ip,
        port,
        path,
        secured,
        "http",
        "https",
        out,
    )
}

pub fn new_ws_url(
    ip: &IpBuffer,
    port: &u16,
    path: &PathBuffer,
    secured: bool,
    out: &mut [u8],
) -> Result<(), ClientError> {
    new_custom_scheme_url(
        ip,
        port,
        path,
        secured,
        "ws",
        "wss",
        out,
    )
}
