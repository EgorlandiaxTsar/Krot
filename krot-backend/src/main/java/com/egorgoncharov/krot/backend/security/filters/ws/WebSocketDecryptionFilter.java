package com.egorgoncharov.krot.backend.security.filters.ws;

import com.egorgoncharov.krot.backend.security.crypto.Cipher;
import com.egorgoncharov.krot.backend.security.crypto.Generator;
import io.vertx.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;
import org.bouncycastle.crypto.InvalidCipherTextException;

@ApplicationScoped
public class WebSocketDecryptionFilter {
    public byte[] decrypt(Buffer frame, byte[] key) throws InvalidCipherTextException {
        int tagIdx = 0;
        int nonceIdx = Cipher.TAG_LEN;
        int cipherIdx = Cipher.TAG_LEN + Generator.NONCE_LEN;
        return Cipher.decrypt(
                frame.getBytes(cipherIdx, frame.length()),
                key,
                frame.getBytes(tagIdx, nonceIdx),
                frame.getBytes(nonceIdx, cipherIdx)
        );
    }
}
