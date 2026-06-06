package com.egorgoncharov.krot.backend.security.session;

import com.egorgoncharov.krot.backend.dto.security.auth.session.RequestSession;
import io.quarkus.security.identity.request.BaseAuthenticationRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SessionAuthenticationRequest extends BaseAuthenticationRequest {
    private final RequestSession session;
}