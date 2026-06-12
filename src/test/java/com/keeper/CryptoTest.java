package com.keeper;

import com.keeper.crypto.Crypto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CryptoTest {

    @Test
    void encryptDecrypt() throws Exception {
        Crypto crypto = new Crypto("masterpassword");
        String original = "my-secret-password";
        String encrypted = crypto.encrypt(original);
        String decrypted = crypto.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void encryptedIsDifferent() throws Exception {
        Crypto crypto = new Crypto("masterpassword");
        String encrypted = crypto.encrypt("test");
        assertNotEquals("test", encrypted);
    }
}