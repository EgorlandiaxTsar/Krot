use crate::client::ws::types::SocketWriter;
use bytes::Bytes;
use std::sync::atomic::AtomicBool;
use std::sync::{Arc, Mutex as SyncMutex};
use tokio::sync::{broadcast, Mutex as AsyncMutex};
use tokio::task::JoinHandle;

#[derive(Clone)]
pub struct WsChannelState {
    pub(crate) alive: Arc<AtomicBool>,
    pub(crate) task: Arc<SyncMutex<Option<JoinHandle<()>>>>,
    pub(crate) events: broadcast::Sender<Bytes>,
    pub(crate) writer: Arc<AsyncMutex<Option<SocketWriter>>>,
}

impl WsChannelState {
    pub fn new(capacity: usize) -> Self {
        let (events, _) = broadcast::channel(capacity);
        Self {
            alive: Arc::new(AtomicBool::new(false)),
            task: Arc::new(SyncMutex::new(None)),
            events,
            writer: Arc::new(AsyncMutex::new(None)),
        }
    }
}
