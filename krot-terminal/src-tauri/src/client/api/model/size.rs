use crate::client::api::model::common::{AuthenticationCredentials, RequestMetadata, ResponseMetadata};
use crate::client::api::model::request::{ApiAuthenticationRequest, DisconnectRequest, WsAuthenticationRequest};
use crate::client::api::model::response::AuthenticationResponse;

pub trait ModelSize: Sized {
    const SIZE: usize = size_of::<Self>();
}

impl ModelSize for ResponseMetadata {}
impl ModelSize for RequestMetadata {}
impl ModelSize for AuthenticationCredentials {}

impl ModelSize for ApiAuthenticationRequest {}
impl ModelSize for WsAuthenticationRequest {}
impl ModelSize for DisconnectRequest {}

impl ModelSize for AuthenticationResponse {}
