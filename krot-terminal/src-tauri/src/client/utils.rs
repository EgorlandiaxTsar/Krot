use crate::client::client::{IpBuffer, PathBuffer, PATH_BUFFER_LEN};
use crate::client::error::ClientError;
use std::io::Write;
use std::time;

macro_rules! endpoint {
    ($name:ident, GET, $path:expr, $red:ty) => {
        pub async fn $name(&self, out: &mut $res) -> Result<(), ClientError> {
            self.req::<(), $res, 0, { $res as JsonSized }::JSON_SIZE>(
                RequestType::Get,
                &new_url($path),
                None,
                true,
                true,
                out,
            ).await
        }
    };

    ($name:ident, $method:ident, $path:expr, $req:ty, $res:ty) => {
        pub async fn $name(&self, body: &$req, out &mut $res) -> Result<(), ClientError> {
            self.req::<$req, $res, { <$req as JsonSized>::JSON_SIZE }, { <$res as JsonSized>::JSON_SIZE } >(
                endpoint!(@method_variant $method),
                &new_url($path),
                Some(body),
                true,
                true,
                out,
            ).await
        }
    };

    (@method_variant POST) => { RequestType::Post };
    (@method_variant PUT) => { RequestType::Put };
    (@method_variant PATCH) => { RequestType::Patch };
}

pub(crate) use endpoint;

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
