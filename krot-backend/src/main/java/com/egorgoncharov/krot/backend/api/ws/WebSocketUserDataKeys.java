package com.egorgoncharov.krot.backend.api.ws;

import com.egorgoncharov.krot.backend.security.session.ws.WebSocketSession;
import io.quarkus.websockets.next.UserData;

public class WebSocketUserDataKeys {
    public static final UserData.TypedKey<WebSocketSession> WS_SESSION = new UserData.TypedKey<>("ws-session");
}
