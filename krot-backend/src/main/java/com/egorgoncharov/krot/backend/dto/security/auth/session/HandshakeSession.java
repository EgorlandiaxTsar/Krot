package com.egorgoncharov.krot.backend.dto.security.auth.session;

import com.egorgoncharov.krot.backend.dto.security.auth.principal.PrincipalType;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@Setter
public class HandshakeSession extends Session {
    private long timestamp;
    private byte[] key;
    private String id;
    private String password;
    private PrincipalType target;

    public HandshakeSession(byte[] body, long timestamp, byte[] key, String id, String password, PrincipalType target) {
        super(body);
        this.timestamp = timestamp;
        this.key = key;
        this.id = id;
        this.password = password;
        this.target = target;
    }

    public boolean isComplete() {
        return id != null && password != null && target != null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HandshakeSession that)) return false;
        return timestamp == that.timestamp && Objects.deepEquals(key, that.key) && Objects.equals(id, that.id) && Objects.equals(password, that.password) && target == that.target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, Arrays.hashCode(key), id, password, target);
    }
}
