use crate::client::error::ClientError;
use crate::client::error::ClientError::ConversionFailed;
use base64::prelude::BASE64_STANDARD;
use base64::Engine;

const HEX_CHARS: &[u8; 16] = b"0123456789abcdef";

pub fn b16uuid_to_string(src: &[u8; 16], out: &mut [u8; 36]) {
    let mut src_idx = 0;
    let mut out_idx = 0;
    while src_idx < 16 {
        if out_idx == 8 || out_idx == 13 || out_idx == 18 || out_idx == 23 {
            out[out_idx] = b'-';
            out_idx += 1;
            continue;
        }
        let byte = src[src_idx];
        out[out_idx] = HEX_CHARS[(byte >> 4) as usize];
        out[out_idx + 1] = HEX_CHARS[(byte & 0x0F) as usize];
        src_idx += 1;
        out_idx += 2;
    }
}

pub fn bytes_to_b64(src: &[u8], out: &mut [u8]) -> Result<usize, ClientError> {
    let required_len = base64::encoded_len(src.len(), true).ok_or(ConversionFailed)?;
    if out.len() < required_len {
        return Err(ConversionFailed);
    }
    let len = BASE64_STANDARD
        .encode_slice(src, out)
        .map_err(|_| ConversionFailed)?;
    Ok(len)
}

pub fn b64_to_bytes(src: &[u8], out: &mut [u8]) -> Result<usize, ClientError> {
    let required_len = base64::decoded_len_estimate(src.len());
    if out.len() < required_len {
        return Err(ConversionFailed);
    }
    let len = BASE64_STANDARD
        .decode_slice(src, out)
        .map_err(|_| ConversionFailed)?;
    Ok(len)
}
