package com.egorgoncharov.krot.backend.dto.security.auth.session;

import com.egorgoncharov.krot.backend.model.entity.SessionEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class RequestSession extends Session {
    private Long timestamp;
    private SessionEntity session;

    public RequestSession(byte[] body, Long timestamp, SessionEntity session) {
        super(body);
        this.timestamp = timestamp;
        this.session = session;
    }

    public boolean isComplete() {
        return timestamp != null && session != null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RequestSession that)) return false;
        return Objects.equals(timestamp, that.timestamp) && Objects.equals(session, that.session);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, session);
    }
}
