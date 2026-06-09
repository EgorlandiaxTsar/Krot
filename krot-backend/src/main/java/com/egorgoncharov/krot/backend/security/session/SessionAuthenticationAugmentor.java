package com.egorgoncharov.krot.backend.security.session;

import com.egorgoncharov.krot.backend.dto.security.auth.principal.Principal;
import com.egorgoncharov.krot.backend.dto.security.auth.principal.PrincipalType;
import com.egorgoncharov.krot.backend.model.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.model.relational.repository.DeviceRepository;
import com.egorgoncharov.krot.backend.model.relational.repository.UserRepository;
import com.egorgoncharov.krot.backend.security.Authority;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class SessionAuthenticationAugmentor implements SecurityIdentityAugmentor {
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    @Inject
    public SessionAuthenticationAugmentor(UserRepository userRepository, DeviceRepository deviceRepository) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
    }

    @WithSession
    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity securityIdentity, AuthenticationRequestContext authenticationRequestContext) {
        if (securityIdentity.isAnonymous()) return Uni.createFrom().item(securityIdentity);
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(securityIdentity);
        Principal principal = securityIdentity.getAttribute("principal");
        return Uni.createFrom().item(principal.getType()).chain(target -> (target == PrincipalType.USER ? userRepository : deviceRepository).findById(UUID.fromString(securityIdentity.getPrincipal().getName()))).map(target -> {
            if (target instanceof UserEntity) {
                ((UserEntity) target).getRole().getAuthorities().forEach(authority -> builder.addPermissionAsString(authority.name()));
            } else {
                builder.addRole(Authority.DEVICE.name());
            }
            principal.setPrincipal(target);
            builder.addAttribute("principal", principal);
            return builder.build();
        });
    }
}
