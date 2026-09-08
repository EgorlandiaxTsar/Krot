#![allow(non_camel_case_types)]

use serde::de::{Error as DeError, SeqAccess, Visitor};
use serde::ser::SerializeSeq;
use serde::{Deserialize, Deserializer, Serialize, Serializer};
use std::fmt;

macro_rules! authority_enum {
    ($($variant:ident),* $(,)?) => {
        #[derive(Debug, Copy, Clone, PartialEq, Eq)]
        pub enum Authority { $($variant,)* }

        impl Authority {
            pub const ALL: &'static [Authority] = &[$(Authority::$variant,)*];
            fn as_str(&self) -> &'static str { match self { $(Authority::$variant => stringify!($variant),)* } }
            fn from_str(s: &str) -> Option<Self> {
                match s { $(stringify!($variant) => Some(Authority::$variant),)* _ => None }
            }
        }
    };
}

authority_enum!(
    DEVICE, X_ROLE_READ, X_USER_READ, X_DEVICE_READ, X_REQUEST_READ, X_SCRIPT_READ, X_SESSION_READ,
    X_ROLE_UPSERT, X_USER_UPSERT, X_ROLE_DELETE, X_USER_DELETE, X_SESSION_DELETE,
    X_DEVICE_TRANSFER_OWNERSHIP, X_SCRIPT_TRANSFER_OWNERSHIP,
    SELF_READ, SELF_UPSERT, SELF_UPSERT_PASSWORD, SELF_DELETE, SELF_SESSION_DELETE,
    SELF_DEVICE_TRANSFER_OWNERSHIP, SELF_SCRIPT_TRANSFER_OWNERSHIP, X_AUTHORITY_MANAGE,
    ROLE_READ, USER_READ, DEVICE_READ, REQUEST_READ, SCRIPT_READ, SESSION_READ,
    ROLE_UPSERT, USER_UPSERT, DEVICE_UPSERT, REQUEST_UPSERT, SCRIPT_UPSERT,
    ROLE_DELETE, USER_DELETE, DEVICE_DELETE, REQUEST_DELETE, SCRIPT_DELETE, SESSION_DELETE,
    DEVICE_TRANSFER_OWNERSHIP, SCRIPT_TRANSFER_OWNERSHIP,
);

pub const AUTHORITY_COUNT: usize = Authority::ALL.len();

#[repr(transparent)]
#[derive(Default, Debug, Copy, Clone)]
pub struct Authorities(u64);

impl Authorities {
    pub fn contains(&self, a: Authority) -> bool { self.0 & (1 << Self::bit(a)) != 0 }
    pub fn insert(&mut self, a: Authority) { self.0 |= 1 << Self::bit(a); }
    fn bit(a: Authority) -> u32 { Authority::ALL.iter().position(|&x| x == a).unwrap() as u32 }
}

impl Serialize for Authorities {
    fn serialize<S: Serializer>(&self, serializer: S) -> Result<S::Ok, S::Error> {
        let mut seq = serializer.serialize_seq(None)?;
        for &a in Authority::ALL {
            if self.contains(a) { seq.serialize_element(a.as_str())?; }
        }
        seq.end()
    }
}

impl<'de> Deserialize<'de> for Authorities {
    fn deserialize<D: Deserializer<'de>>(deserializer: D) -> Result<Self, D::Error> {
        struct V;
        impl<'de> Visitor<'de> for V {
            type Value = Authorities;
            fn expecting(&self, f: &mut fmt::Formatter) -> fmt::Result { f.write_str("an array of authority strings") }
            fn visit_seq<A: SeqAccess<'de>>(self, mut seq: A) -> Result<Authorities, A::Error> {
                let mut out = Authorities::default();
                while let Some(s) = seq.next_element::<&str>()? {
                    out.insert(Authority::from_str(s).ok_or_else(|| DeError::custom(format!("unknown authority: {s}")))?);
                }
                Ok(out)
            }
        }
        deserializer.deserialize_seq(V)
    }
}

impl super::size::JsonSized for Authorities {
    const JSON_SIZE: usize = 2 + AUTHORITY_COUNT * 32;
}