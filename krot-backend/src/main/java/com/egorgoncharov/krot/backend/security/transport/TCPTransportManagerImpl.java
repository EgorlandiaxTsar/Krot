package com.egorgoncharov.krot.backend.security.transport;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.config.yaml.HandshakeConfig;
import com.egorgoncharov.krot.backend.config.yaml.STPConfig;
import com.egorgoncharov.krot.backend.database.redis.repository.SessionRepository;
import com.egorgoncharov.krot.backend.security.crypto.Cipher;
import com.egorgoncharov.krot.backend.security.crypto.Ephemeral;
import com.egorgoncharov.krot.backend.security.crypto.Generator;
import com.egorgoncharov.krot.backend.security.session.principal.PrincipalType;
import com.egorgoncharov.krot.backend.security.transport.headers.HandshakeHeaders;
import com.egorgoncharov.krot.backend.security.transport.headers.RequestHeaders;
import com.egorgoncharov.krot.backend.security.transport.response.EncryptedResponse;
import com.egorgoncharov.krot.backend.security.transport.session.HandshakeSession;
import com.egorgoncharov.krot.backend.security.transport.session.RequestSession;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@ApplicationScoped
public class TCPTransportManagerImpl implements TCPTransportManager {
    private final STPConfig stpConfig;
    private final HandshakeConfig handshakeConfig;
    private final SessionRepository sessionRepository;

    @Inject
    public TCPTransportManagerImpl(STPConfig stpConfig, HandshakeConfig handshakeConfig, SessionRepository sessionRepository) {
        this.stpConfig = stpConfig;
        this.handshakeConfig = handshakeConfig;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Result<HandshakeSession> establishHandshakeRequest(Buffer body, HandshakeHeaders headers) {
        if (!headers.isComplete()) {
            return Result.badRequest();
        }
        try {
            byte[] clientPublicKey = headers.getClientPubkey();
            byte[] serverPrivateKey = Base64.getDecoder().decode(handshakeConfig.privateKey());
            byte[] sharedSecret = Ephemeral.calculateX25519(serverPrivateKey, clientPublicKey);
            byte[] handshakeKey = Ephemeral.deriveHandshakeKey(sharedSecret);
            byte[] bytesBody = body.getBytes();
            try {
                byte[] decrypted = Cipher.decrypt(bytesBody, handshakeKey, headers.getTag(), headers.getNonce());
                JsonObject json = new JsonObject(Buffer.buffer(decrypted));
                HandshakeSession session = new HandshakeSession(decrypted, json.getLong("timestamp"), handshakeKey, json.getString("identifier"), json.getString("password"), PrincipalType.valueOf(json.getString("target")));
                if (!session.isComplete()) return Result.badRequest();
                if (Math.abs(session.getTimestamp() - Instant.now().toEpochMilli()) > handshakeConfig.nonceWindow()) return Result.forbidden();
                return Result.ok(session);
            } catch (Exception e) {
                return Result.forbidden();
            }
        } catch (Exception e) {
            return Result.badRequest();
        }
    }

    @WithSession
    @Override
    public Uni<Result<RequestSession>> establishRequest(Buffer body, RequestHeaders headers) {
        if (!headers.isComplete()) {
            return Uni.createFrom().item(Result.badRequest());
        }
        return sessionRepository.findBySessionReference(UUID.fromString(headers.getSessionReference())).chain(session -> {
            if (session == null || session.getValidUntil().isBefore(OffsetDateTime.now())) return Uni.createFrom().item(Result.forbidden());
            try {
                byte[] decrypted = stpConfig.enableIncoming() ? Cipher.decrypt(body.getBytes(), session.getEncryptionKey(), headers.getTag(), headers.getNonce()) : body.getBytes();
                JsonObject json = new JsonObject(Buffer.buffer(decrypted));
                if (!json.getJsonObject("metadata").getString("sessionId").equals(session.getId().toString())) return Uni.createFrom().item(Result.forbidden());
                RequestSession requestSession = new RequestSession(decrypted, json.getJsonObject("metadata").getLong("timestamp"), session);
                if (!requestSession.isComplete()) return Uni.createFrom().item(Result.badRequest());
                return Uni.createFrom().item(Result.ok(requestSession));
            } catch (Exception e) {
                return Uni.createFrom().item(Result.unauthorized());
            }
        });
    }

    @Override
    public Result<EncryptedResponse> encryptResponse(byte[] body, byte[] key) {
        try {
            byte[] nonce = Generator.generateNonce();
            byte[] encrypted = Cipher.encrypt(body, key, nonce);
            byte[] ciphertext = Arrays.copyOfRange(encrypted, 0, encrypted.length - Cipher.TAG_LEN);
            byte[] tag = Arrays.copyOfRange(encrypted, encrypted.length - Cipher.TAG_LEN, encrypted.length);
            return Result.ok(new EncryptedResponse(ciphertext, Base64.getEncoder().encodeToString(tag), Base64.getEncoder().encodeToString(nonce)));
        } catch (Exception e) {
            return Result.error();
        }
    }
}
