pub mod serialize {
    pub(crate) use crate::client::api::model::common::IdList;
    use crate::client::types::converters::HEX_CHARS;
    use serde::ser::SerializeSeq;
    use serde::Serializer;

    pub fn varchar<const N: usize, S: Serializer>(
        data: &[u8; N],
        serializer: S,
    ) -> Result<S::Ok, S::Error> {
        let len = data.iter().position(|&b| b == 0).unwrap_or(N);
        let s = std::str::from_utf8(&data[..len]).map_err(serde::ser::Error::custom)?;
        serializer.serialize_str(s)
    }

    pub fn varchar_opt<const N: usize, S: Serializer>(
        data: &Option<[u8; N]>,
        serializer: S,
    ) -> Result<S::Ok, S::Error> {
        match data {
            Some(bytes) => varchar(bytes, serializer),
            None => serializer.serialize_none(),
        }
    }

    pub fn uuid<S: Serializer>(
        data: &[u8; 16],
        serializer: S,
    ) -> Result<S::Ok, S::Error> {
        let mut buf = [0u8; 36];
        uuid_to_hyphenated(data, &mut buf);
        let uuid_str = std::str::from_utf8(&buf).map_err(|e| serde::ser::Error::custom(e.to_string()))?;
        serializer.serialize_str(uuid_str)
    }

    pub fn uuid_opt<S: Serializer>(data: &Option<[u8; 16]>, serializer: S) -> Result<S::Ok, S::Error> {
        match data {
            Some(id) => uuid(id, serializer),
            None => serializer.serialize_none(),
        }
    }

    pub fn uuid_vec<S: Serializer>(data: &[[u8; 16]], serializer: S) -> Result<S::Ok, S::Error> {
        let mut seq = serializer.serialize_seq(Some(data.len()))?;
        for id in data {
            let mut buf = [0u8; 36];
            uuid_to_hyphenated(id, &mut buf);
            seq.serialize_element(std::str::from_utf8(&buf).map_err(serde::ser::Error::custom)?)?;
        }
        seq.end()
    }

    pub fn uuid_vec_opt<S: Serializer>(data: &Option<IdList>, serializer: S) -> Result<S::Ok, S::Error> {
        match data {
            Some(ids) => uuid_vec(ids, serializer),
            None => serializer.serialize_none(),
        }
    }

    fn uuid_to_hyphenated(id: &[u8; 16], buf: &mut [u8; 36]) {
        buf[8] = b'-';
        buf[13] = b'-';
        buf[18] = b'-';
        buf[23] = b'-';
        let mut write_hex = |idx: usize, byte: u8| {
            buf[idx] = HEX_CHARS[(byte >> 4) as usize];
            buf[idx + 1] = HEX_CHARS[(byte & 0x0F) as usize];
        };
        write_hex(0, id[0]);
        write_hex(2, id[1]);
        write_hex(4, id[2]);
        write_hex(6, id[3]);
        write_hex(9, id[4]);
        write_hex(11, id[5]);
        write_hex(14, id[6]);
        write_hex(16, id[7]);
        write_hex(19, id[8]);
        write_hex(21, id[9]);
        write_hex(24, id[10]);
        write_hex(26, id[11]);
        write_hex(28, id[12]);
        write_hex(30, id[13]);
        write_hex(32, id[14]);
        write_hex(34, id[15]);
    }
}

pub mod deserialize {
    use base64::prelude::BASE64_STANDARD;
    use base64::Engine;
    use serde::de::{Error, SeqAccess, Visitor};
    use serde::{Deserialize, Deserializer};
    use std::fmt;

    pub fn varchar<'de, const N: usize, D: Deserializer<'de>>(deserializer: D) -> Result<[u8; N], D::Error> {
        let s = <&str>::deserialize(deserializer)?;
        let bytes = s.as_bytes();
        if bytes.len() > N {
            return Err(D::Error::custom(format!("string too long: got {} bytes, buffer holds {}", bytes.len(), N)));
        }
        let mut buf = [0u8; N];
        buf[..bytes.len()].copy_from_slice(bytes);
        Ok(buf)
    }

    pub fn varchar_opt<'de, const N: usize, D: Deserializer<'de>>(deserializer: D) -> Result<Option<[u8; N]>, D::Error> {
        let opt = Option::<&str>::deserialize(deserializer)?;
        match opt {
            Some(s) => {
                let bytes = s.as_bytes();
                if bytes.len() > N {
                    return Err(D::Error::custom(format!("string too long: got {} bytes, buffer holds {}", bytes.len(), N)));
                }
                let mut buf = [0u8; N];
                buf[..bytes.len()].copy_from_slice(bytes);
                Ok(Some(buf))
            }
            None => Ok(None),
        }
    }

    pub fn b32_encryption_key<'de, D: Deserializer<'de>>(deserializer: D) -> Result<[u8; 32], D::Error> {
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
        let data = <&str>::deserialize(deserializer)?;
        parse_uuid(data.as_bytes()).map_err(D::Error::custom)
    }

    pub fn uuid_vec<'de, D: Deserializer<'de>>(deserializer: D) -> Result<super::serialize::IdList, D::Error> {
        struct V;
        impl<'de> Visitor<'de> for V {
            type Value = super::serialize::IdList;
            fn expecting(&self, f: &mut fmt::Formatter) -> fmt::Result {
                f.write_str("an array of UUID strings")
            }
            fn visit_seq<A: SeqAccess<'de>>(self, mut seq: A) -> Result<Self::Value, A::Error> {
                let mut out = Vec::with_capacity(seq.size_hint().unwrap_or(0));
                while let Some(s) = seq.next_element::<&str>()? {
                    out.push(parse_uuid(s.as_bytes()).map_err(A::Error::custom)?);
                }
                Ok(out)
            }
        }
        deserializer.deserialize_seq(V)
    }

    fn parse_uuid(data: &[u8]) -> Result<[u8; 16], &'static str> {
        if data.len() != 36 {
            return Err("UUID string must be exactly 36 characters");
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
                return Err("UUID out of range");
            }
            let hex_pair = std::str::from_utf8(&data[idx..idx + 2]).map_err(|_| "invalid UTF-8 in UUID")?;
            buf[buf_idx] = u8::from_str_radix(hex_pair, 16).map_err(|_| "invalid hex in UUID")?;
            buf_idx += 1;
            idx += 2;
        }
        if buf_idx != 16 {
            return Err("UUID out of range");
        }
        Ok(buf)
    }
}
