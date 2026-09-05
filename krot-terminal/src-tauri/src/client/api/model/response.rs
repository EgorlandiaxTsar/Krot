use crate::client::api::model::common::{AuthenticationCredentials, ResponseMetadata};
use serde::Deserialize;

#[repr(C)]
#[derive(Default, Debug, Copy, Clone, Deserialize)]
pub struct AuthenticationResponse {
    pub metadata: ResponseMetadata,
    pub data: AuthenticationCredentials,
}
