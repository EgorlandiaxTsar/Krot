pub mod serialize {
    use crate::client::types::converters::HEX_CHARS;
    use serde::Serializer;

    pub fn varchar<const N: usize, S: Serializer>(
        data: &[u8; N],
        serializer: S,
    ) -> Result<S::Ok, S::Error> {
        let len = data.iter().position(|&b| b == 0).unwrap_or(N);
        let s = std::str::from_utf8(&data[..len]).map_err(serde::ser::Error::custom)?;
        serializer.serialize_str(s)
    }

    pub fn uuid<S: Serializer>(
        data: &[u8; 16],
        serializer: S,
    ) -> Result<S::Ok, S::Error> {
        let mut buf = [0u8; 36];
        buf[8] = b'-';
        buf[13] = b'-';
        buf[18] = b'-';
        buf[23] = b'-';
        let mut write_hex = |idx: usize, byte: u8| {
            buf[idx] = HEX_CHARS[(byte >> 4) as usize];
            buf[idx + 1] = HEX_CHARS[(byte & 0x0F) as usize];
        };
        write_hex(0, data[0]);
        write_hex(2, data[1]);
        write_hex(4, data[2]);
        write_hex(6, data[3]);
        write_hex(9, data[4]);
        write_hex(11, data[5]);
        write_hex(14, data[6]);
        write_hex(16, data[7]);
        write_hex(19, data[8]);
        write_hex(21, data[9]);
        write_hex(24, data[10]);
        write_hex(26, data[11]);
        write_hex(28, data[12]);
        write_hex(30, data[13]);
        write_hex(32, data[14]);
        write_hex(34, data[15]);
        let uuid_str = std::str::from_utf8(&buf).map_err(|e| serde::ser::Error::custom(e.to_string()))?;
        serializer.serialize_str(uuid_str)
    }
}

pub mod deserialize {
    use base64::prelude::BASE64_STANDARD;
    use base64::Engine;
    use serde::de::Error;
    use serde::{Deserialize, Deserializer};

    pub fn b32_encryption_key<'de, D: Deserializer<'de>>(
        deserializer: D,
    ) -> Result<[u8; 32], D::Error> {
        let data = <&str>::deserialize(deserializer)?;
        let mut buf = [0u8; 32];
        let len = BASE64_STANDARD
            .decode_slice(data.as_bytes(), &mut buf)
            .map_err(|e| D::Error::custom(format!("Base64 decoding failed: {}", e)))?;
        if len != 32 {
            return Err(D::Error::custom(format!(
                "Invalid output bytes length, expected 32, got {}",
                len
            )));
        }
        Ok(buf)
    }

    pub fn uuid<'de, D: Deserializer<'de>>(deserializer: D) -> Result<[u8; 16], D::Error> {
        let data = <&str>::deserialize(deserializer)?.as_bytes();
        if data.len() != 36 {
            return Err(D::Error::custom(
                "UUID string must be exactly 32 hex characters",
            ));
        }
        let mut buf = [0u8; 16];
        let mut buf_idx = 0;
        let mut idx = 0;
        while idx < data.len() {
            if data[idx] == b'-' {
                idx += 1;
                continue;
            }
            if buf_idx >= 16 {
                return Err(D::Error::custom("UUID out of range"));
            }
            let hex_pair = std::str::from_utf8(&data[idx..idx + 2])
                .map_err(|e| D::Error::custom(format!("UTF8 conversion failed: {}", e)))?;
            buf[buf_idx] = u8::from_str_radix(hex_pair, 16)
                .map_err(|e| D::Error::custom(format!("Invalid hex string syntax: {}", e)))?;
            buf_idx += 1;
            idx += 2;
        }
        if buf_idx != 16 {
            return Err(D::Error::custom("UUID out of range"));
        }
        Ok(buf)
    }
}
