package com.egorgoncharov.krot.backend.security.auth;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.database.redis.entity.SessionEntity;
import io.smallrye.mutiny.Uni;

import java.util.UUID;

public interface AuthenticationManager {
    Uni<Result<SessionEntity>> login(SessionEntity metadata, String identifier, String password);

    Uni<Result<SessionEntity>> logout(UUID sessionId);
}
