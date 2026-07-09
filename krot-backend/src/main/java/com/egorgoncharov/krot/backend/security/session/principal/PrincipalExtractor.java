package com.egorgoncharov.krot.backend.security.session.principal;

import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import io.quarkus.security.identity.SecurityIdentity;

public class PrincipalExtractor {
    public static UserEntity principal(SecurityIdentity client) {
        Principal principal = client.getAttribute("principal");
        return client.isAnonymous() || principal.getType() == PrincipalType.DEVICE ? null : principal.getPrincipal();
    }
}
