package com.egorgoncharov.krot.backend.security.session;

import com.egorgoncharov.krot.backend.dto.security.auth.principal.Principal;
import com.egorgoncharov.krot.backend.dto.security.auth.principal.PrincipalType;
import com.egorgoncharov.krot.backend.dto.security.auth.session.RequestSession;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SessionIdentityProvider implements IdentityProvider<SessionAuthenticationRequest> {

    @Override
    public Class<SessionAuthenticationRequest> getRequestType() {
        return SessionAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(SessionAuthenticationRequest request, AuthenticationRequestContext context) {
        RequestSession session = request.getSession();
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder();
        Principal principal = new Principal();
        builder.setPrincipal(new QuarkusPrincipal(session.getSession().getOwnerId().toString()));
        principal.setType(session.getSession().isOwnerDevice() ? PrincipalType.DEVICE : PrincipalType.USER);
        builder.addAttribute("principal", principal);
        builder.addAttribute("session", session);
        return Uni.createFrom().item(builder.build());
    }
}