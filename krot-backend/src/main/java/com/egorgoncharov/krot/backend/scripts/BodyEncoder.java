package com.egorgoncharov.krot.backend.scripts;

import com.egorgoncharov.krot.backend.service.helper.SecurityHelper;

import java.util.Arrays;
import java.util.Base64;

public class BodyEncoder {
    private static final String SERVER_PUBLIC = "zSkMxt/GdIKUV4jsbOORZnRKrafMbkVXCc1CdlFvDCA=";
    private static final String CLIENT_PRIVATE = "MHTK+91exs1pkDrnHzza6nEjqFLQ9cTIsd3vr0Y1dU0=";

    public static void main(String[] args) {
        try {
            byte[] serverPubKey = Base64.getDecoder().decode(SERVER_PUBLIC);
            byte[] clientPrivKey = Base64.getDecoder().decode(CLIENT_PRIVATE);
            byte[] sharedSecret = SecurityHelper.calculateX25519(clientPrivKey, serverPubKey);
            byte[] handshakeKey = SecurityHelper.deriveHandshakeKey(sharedSecret);
            String json = """
                    {
                        "id": "a6a664de-ca91-4b92-b5cd-adbbb9eec46b",
                        "password": "132q085ZZ07646hq948w05lb7g4o1500",
                        "timestamp": 1772223439505,
                        "target": "USER"
                    }
                    """;
            byte[] plaintext = json.getBytes();
            byte[] nonce = SecurityHelper.generateNonce();
            byte[] encryptedWithTag = SecurityHelper.encrypt(plaintext, handshakeKey, nonce);
            int ciphertextLen = encryptedWithTag.length - SecurityHelper.TAG_LEN;
            byte[] bodyOnly = Arrays.copyOfRange(encryptedWithTag, 0, ciphertextLen);
            byte[] tagOnly = Arrays.copyOfRange(encryptedWithTag, ciphertextLen, encryptedWithTag.length);
            System.out.println("--- Test Data for Handshake ---");
            System.out.println("X-Nonce-Header: " + Base64.getEncoder().encodeToString(nonce));
            System.out.println("X-Tag-Header:   " + Base64.getEncoder().encodeToString(tagOnly));
            System.out.println("Request Body:   " + Base64.getEncoder().encodeToString(bodyOnly));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
