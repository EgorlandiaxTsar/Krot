use crate::client::api::model::{AuthenticationCredentials, Metadata};
use serde::Deserialize;

pub const AUTHENTICATION_RESPONSE_SIZE: usize = size_of::<AuthenticationResponse>();

#[repr(C)]
#[derive(Default, Debug, Copy, Clone, Deserialize)]
pub struct AuthenticationResponse {
    pub metadata: Metadata,
    pub data: AuthenticationCredentials,
}
