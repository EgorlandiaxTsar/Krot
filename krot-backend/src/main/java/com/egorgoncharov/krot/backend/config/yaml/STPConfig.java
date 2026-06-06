package com.egorgoncharov.krot.backend.config.yaml;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app.security.secured-transport-protocol")
public interface STPConfig {
    boolean enableIncoming();

    boolean enableOutcoming();
}
