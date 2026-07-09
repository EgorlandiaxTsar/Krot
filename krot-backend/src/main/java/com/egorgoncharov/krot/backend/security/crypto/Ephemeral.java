package com.egorgoncharov.krot.backend.security.crypto;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;

public class Ephemeral {
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
}
