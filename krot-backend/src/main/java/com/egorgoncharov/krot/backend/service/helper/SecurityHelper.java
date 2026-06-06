package com.egorgoncharov.krot.backend.service.helper;

import com.egorgoncharov.krot.backend.dto.security.auth.principal.Principal;
import com.egorgoncharov.krot.backend.dto.security.auth.principal.PrincipalType;
import com.egorgoncharov.krot.backend.model.entity.RoleEntity;
import com.egorgoncharov.krot.backend.model.entity.UserEntity;
import io.quarkus.security.identity.SecurityIdentity;
import org.apache.commons.lang3.RandomStringUtils;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.*;

import java.security.SecureRandom;

public class SecurityHelper {
    public static final int NONCE_LEN = 12; // Bytes
    public static final int TAG_LEN = 16;   // Bytes
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static RoleEntity securityIdentityRole(SecurityIdentity client) {
        UserEntity clientUser = securityIdentityUser(client);
        return clientUser == null ? null : clientUser.getRole();
    }

    public static UserEntity securityIdentityUser(SecurityIdentity client) {
        Principal principal = client.getAttribute("principal");
        return client.isAnonymous() || principal.getType() == PrincipalType.DEVICE ? null : principal.getPrincipal();
    }

    public static byte[] calculateX25519(byte[] privateKey, byte[] publicKey) {
        X25519PrivateKeyParameters privateKeyParameters = new X25519PrivateKeyParameters(privateKey, 0);
        X25519PublicKeyParameters publicKeyParameters = new X25519PublicKeyParameters(publicKey, 0);
        byte[] secret = new byte[32];
        privateKeyParameters.generateSecret(publicKeyParameters, secret, 0);
        return secret;
    }

    public static byte[] deriveHandshakeKey(byte[] sharedSecret) {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(sharedSecret, null, "handshake-v1".getBytes()));
        byte[] okm = new byte[32];
        hkdf.generateBytes(okm, 0, 32);
        return okm;
    }

    public static byte[] decrypt(byte[] ciphertext, byte[] key, byte[] tag, byte[] nonce) throws InvalidCipherTextException {
        byte[] input = new byte[ciphertext.length + tag.length];
        System.arraycopy(ciphertext, 0, input, 0, ciphertext.length);
        System.arraycopy(tag, 0, input, ciphertext.length, tag.length);
        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        cipher.init(false, new ParametersWithIV(new KeyParameter(key), nonce));
        byte[] out = new byte[cipher.getOutputSize(input.length)];
        int len = cipher.processBytes(input, 0, input.length, out, 0);
        cipher.doFinal(out, len);
        return out;
    }

    public static byte[] encrypt(byte[] text, byte[] key, byte[] nonce) {
        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        KeyParameter keyParameter = new KeyParameter(key);
        ParametersWithIV parameters = new ParametersWithIV(keyParameter, nonce);
        cipher.init(true, parameters);
        byte[] out = new byte[text.length + TAG_LEN];
        int len = cipher.processBytes(text, 0, text.length, out, 0);
        try {
            cipher.doFinal(out, len);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return out;
    }

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
                    password.append(RandomStringUtils.randomAlphabetic(1));
                    continue;
                } else {
                    charType++;
                }
            }
            if (charType == 1) {
                if (includeSymbols) {
                    password.append(RandomStringUtils.randomAlphanumeric(1));
                    continue;
                } else {
                    charType++;
                }
            }
            if (charType == 2 && includeDigits) {
                password.append(RandomStringUtils.randomNumeric(1));
            }
        }
        return password.isEmpty() ? null : password.toString();
    }
}
