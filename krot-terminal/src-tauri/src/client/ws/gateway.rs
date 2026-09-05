use crate::client::client::PATH_BUFFER_LEN;
use crate::client::secret::credentials::CredentialsHolder;
use crate::client::secret::session::SessionHolder;
use crate::client::ws::channel::EventsChannel;
use std::sync::Arc;

pub const WS_EVENT_CHANNEL_CAPACITY: usize = 256;
pub const WS_PATH_BUFFER_LEN: usize = PATH_BUFFER_LEN + 20;
pub const WS_AUTHENTICATION_CONTENT_MAX_LEN: usize = 2048;

pub struct WsGateway {
    pub events: EventsChannel,
}

impl WsGateway {
    pub fn new(session: Arc<SessionHolder>, credentials: Arc<CredentialsHolder>) -> Self {
        Self {
            events: EventsChannel::new(session, credentials)
        }
    }
}
