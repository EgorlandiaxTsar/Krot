pub mod serialize {
    use serde::Serializer;

    pub fn varchar<const N: usize, S: Serializer>(
        data: &[u8; N],
        serializer: S,
    ) -> Result<S::Ok, S::Error> {
        let len = data.iter().position(|&b| b == 0).unwrap_or(N);
        let s = std::str::from_utf8(&data[..len]).map_err(serde::ser::Error::custom)?;
        serializer.serialize_str(s)
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
        if data.len() != 32 {
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
            let hex_pair = std::str::from_utf8(&buf[idx..idx + 2])
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
