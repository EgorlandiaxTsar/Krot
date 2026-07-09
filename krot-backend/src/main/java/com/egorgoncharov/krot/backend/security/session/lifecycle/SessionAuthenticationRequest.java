package com.egorgoncharov.krot.backend.security.session.lifecycle;

import com.egorgoncharov.krot.backend.security.transport.session.RequestSession;
import io.quarkus.security.identity.request.BaseAuthenticationRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SessionAuthenticationRequest extends BaseAuthenticationRequest {
    private final RequestSession session;
}