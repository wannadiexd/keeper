package com.keeper;

import com.keeper.crypto.Crypto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CryptoTest {

    @Test
    void testEncryptDecrypt() throws Exception {
        byte[] salt = Crypto.generateSalt();
        Crypto crypto = new Crypto("testpassword", salt);
        String original = "mySecretPassword";
        String encrypted = crypto.encrypt(original);
        String decrypted = crypto.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void testDifferentSaltsProduceDifferentKeys() throws Exception {
        byte[] salt1 = Crypto.generateSalt();
        byte[] salt2 = Crypto.generateSalt();
        Crypto crypto1 = new Crypto("password", salt1);
        Crypto crypto2 = new Crypto("password", salt2);
        String encrypted = crypto1.encrypt("hello");
        assertThrows(Exception.class, () -> crypto2.decrypt(encrypted));
    }
}