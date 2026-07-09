package com.egorgoncharov.krot.backend.security.transport.session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Session {
    private byte[] body;
}
