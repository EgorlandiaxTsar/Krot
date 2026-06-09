package com.egorgoncharov.krot.backend.service;

import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.model.redis.entity.SessionEntity;
import io.smallrye.mutiny.Uni;

import java.util.UUID;

public interface AuthenticationService {
    Uni<Result<SessionEntity>> login(SessionEntity metadata, String password);

    Uni<Result<SessionEntity>> logout(UUID sessionId);
}
