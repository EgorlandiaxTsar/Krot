package com.egorgoncharov.krot.backend.security.session.ws;

import com.egorgoncharov.krot.backend.security.transport.session.RequestSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketSession {
    private UUID userId;
    private RequestSession session;
}

