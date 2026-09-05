use futures_util::stream::{SplitSink, SplitStream};
use tokio::net::TcpStream;
use tokio_tungstenite::tungstenite::Message;
use tokio_tungstenite::{MaybeTlsStream, WebSocketStream};

pub type Socket = WebSocketStream<MaybeTlsStream<TcpStream>>;
pub type SocketWriter = SplitSink<Socket, Message>;
pub type SocketReader = SplitStream<Socket>;
