package com.egorgoncharov.krot.backend.security.crypto;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public class Cipher {
    public static final int TAG_LEN = 16;   // Bytes

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
}
