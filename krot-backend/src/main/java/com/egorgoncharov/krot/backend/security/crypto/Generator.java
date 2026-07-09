package com.egorgoncharov.krot.backend.security.crypto;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;

public class Generator {
    public static final int NONCE_LEN = 12; // Bytes
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static byte[] generateXCC20Key() {
        byte[] key = new byte[32];
        SECURE_RANDOM.nextBytes(key);
        return key;
    }

    public static byte[] generateNonce() {
        byte[] nonce = new byte[NONCE_LEN];
        SECURE_RANDOM.nextBytes(nonce);
        return nonce;
    }

    public static String generateRandomPassword(int length, boolean includeLetters, boolean includeDigits, boolean includeSymbols) {
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int charType = SECURE_RANDOM.nextInt(0, 3);
            if (charType == 0) {
                if (includeLetters) {
                    password.append(RandomStringUtils.secureStrong().nextAlphabetic(1));
                    continue;
                } else {
                    charType++;
                }
            }
            if (charType == 1) {
                if (includeSymbols) {
                    password.append(RandomStringUtils.secureStrong().nextAlphanumeric(1));
                    continue;
                } else {
                    charType++;
                }
            }
            if (charType == 2 && includeDigits) {
                password.append(RandomStringUtils.secureStrong().nextNumeric(1));
            }
        }
        return password.isEmpty() ? null : password.toString();
    }
}
