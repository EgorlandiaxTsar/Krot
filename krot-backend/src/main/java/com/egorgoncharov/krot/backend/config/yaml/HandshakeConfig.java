package com.egorgoncharov.krot.backend.config.yaml;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app.security.handshake")
public interface HandshakeConfig {
    String privateKey();

    String publicKey();

    int nonceWindow();
}
