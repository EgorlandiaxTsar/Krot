package com.egorgoncharov.krot.backend.dto.security.transport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class EncryptedResponse {
    private final byte[] body;
    private final String tag;
    private final String nonce;
}
