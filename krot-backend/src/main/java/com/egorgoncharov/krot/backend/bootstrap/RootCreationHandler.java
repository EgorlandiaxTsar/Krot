package com.egorgoncharov.krot.backend.bootstrap;

import com.egorgoncharov.krot.backend.model.relational.entity.RoleEntity;
import com.egorgoncharov.krot.backend.model.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.model.relational.repository.RoleRepository;
import com.egorgoncharov.krot.backend.model.relational.repository.UserRepository;
import com.egorgoncharov.krot.backend.security.Authority;
import com.egorgoncharov.krot.backend.service.helper.SecurityHelper;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.runtime.Startup;
import io.quarkus.vertx.VertxContextSupport;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.Arrays;

@ApplicationScoped
public class RootCreationHandler {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Inject
    public RootCreationHandler(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Startup
    public void init() throws Throwable {
        VertxContextSupport.subscribeAndAwait(() -> Panache.withTransaction(() -> roleRepository.findByName("root").onItem().ifNull().switchTo(() -> roleRepository.save(RoleEntity.builder().name("root").grade(Integer.MAX_VALUE).authorities(Arrays.stream(Authority.values()).filter(e -> e != Authority.DEVICE).toList()).build())).chain(role -> userRepository.findByName("root").onItem().ifNull().switchTo(() -> {
            String generatedPassword = SecurityHelper.generateRandomPassword(32, true, true, false);
            System.out.println("GENERATED ROOT PASSWORD: " + generatedPassword);
            return userRepository.save(UserEntity.builder().username("root").password(generatedPassword).role(role).active(true).createdAt(OffsetDateTime.now()).build());
        }))).replaceWithVoid());
    }
}
