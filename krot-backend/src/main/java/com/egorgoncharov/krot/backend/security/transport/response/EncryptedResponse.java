package com.egorgoncharov.krot.backend.security.transport.response;

public record EncryptedResponse(byte[] body, String tag, String nonce) {
}
