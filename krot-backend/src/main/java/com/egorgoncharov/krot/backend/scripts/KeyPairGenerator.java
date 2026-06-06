package com.egorgoncharov.krot.backend.scripts;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator;
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

public class KeyPairGenerator {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) {
        X25519KeyPairGenerator generator = new X25519KeyPairGenerator();
        generator.init(new X25519KeyGenerationParameters(new SecureRandom()));
        AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();
        X25519PrivateKeyParameters privateKey = (X25519PrivateKeyParameters) keyPair.getPrivate();
        X25519PublicKeyParameters publicKey = (X25519PublicKeyParameters) keyPair.getPublic();
        byte[] privBytes = privateKey.getEncoded();
        byte[] pubBytes = publicKey.getEncoded();
        System.out.println("--- Generated X25519 Keypair ---");
        System.out.println("Private Key (Base64): " + Base64.getEncoder().encodeToString(privBytes));
        System.out.println("Public Key (Base64):  " + Base64.getEncoder().encodeToString(pubBytes));
    }
}