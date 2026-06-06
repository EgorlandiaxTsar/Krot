package com.egorgoncharov.krot.backend.config.yaml;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app.security.session")
public interface SessionConfig {
    int sessionDuration();
}
