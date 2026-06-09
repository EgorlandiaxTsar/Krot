package com.egorgoncharov.krot.backend.security.session;

import com.egorgoncharov.krot.backend.config.yaml.HandshakeConfig;
import com.egorgoncharov.krot.backend.dto.security.auth.session.HandshakeSession;
import com.egorgoncharov.krot.backend.dto.security.auth.session.RequestSession;
import com.egorgoncharov.krot.backend.dto.security.transport.headers.RequestHeaders;
import com.egorgoncharov.krot.backend.model.redis.repository.SessionRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;

@ApplicationScoped
public class SessionAuthenticationMechanism implements HttpAuthenticationMechanism {
    private final HandshakeConfig handshakeConfig;
    private final SessionRepository sessionRepository;

    @Inject
    public SessionAuthenticationMechanism(HandshakeConfig handshakeConfig, SessionRepository historicalSessionRepository) {
        this.handshakeConfig = handshakeConfig;
        this.sessionRepository = historicalSessionRepository;
    }

    @WithTransaction
    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        HandshakeSession handshake = context.get("handshakeSession");
        if (handshake != null) return Uni.createFrom().item(QuarkusSecurityIdentity.builder().setAnonymous(true).addAttribute("handshakeSession", handshake).build());
        RequestSession requestSession = context.get("requestSession");
        if (requestSession != null) {
            if (Math.abs(requestSession.getSession().getLastUsed().toInstant().toEpochMilli() - requestSession.getTimestamp()) > handshakeConfig.nonceWindow() || Math.abs(requestSession.getTimestamp() - Instant.now().toEpochMilli()) > handshakeConfig.nonceWindow()) {
                return Uni.createFrom().failure(new AuthenticationFailedException());
            }
            requestSession.getSession().setLastUsed(OffsetDateTime.now());
            return sessionRepository.save(requestSession.getSession()).chain(() -> identityProviderManager.authenticate(new SessionAuthenticationRequest(requestSession)));
        }
        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().nullItem();
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Collections.singleton(SessionAuthenticationRequest.class);
    }

    @Override
    public Uni<Boolean> sendChallenge(RoutingContext context) {
        context.response().setStatusCode(401).end();
        return Uni.createFrom().item(true);
    }

    @Override
    public Uni<HttpCredentialTransport> getCredentialTransport(RoutingContext context) {
        return Uni.createFrom().item(new HttpCredentialTransport(HttpCredentialTransport.Type.OTHER_HEADER, RequestHeaders.SESSION_REFERENCE_HEADER_NAME));
    }

    @Override
    public int getPriority() {
        return 1000;
    }
}
