package no.ntnu.onion.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

class DiffieHellmanTest {

    @Test
    @DisplayName("Successful Key Exchange")
    public void successfulKeyExchange() {
        try {
            DiffieHellman diffieHellman1 = new DiffieHellman();
            DiffieHellman diffieHellman2 = new DiffieHellman();

            // Initialize first key
            byte[] encodedPublicKey1 = diffieHellman1.initializeKeyPair();

            // A -> B
            PublicKey publicKey1 = diffieHellman2.instantiatePublicKey(encodedPublicKey1);
            byte[] encodedPublicKey2 = diffieHellman2.initializeKeyPair(publicKey1);
            // Send encoded public key. Afterwards we can generate the secret
            byte[] secret1 =  diffieHellman2.generateSecret(publicKey1);

            // B -> A
            PublicKey publicKey2 = diffieHellman2.instantiatePublicKey(encodedPublicKey2);
            byte[] secret2 = diffieHellman1.generateSecret(publicKey2);

            assertEquals(DiffieHellman.toHexString(secret1), DiffieHellman.toHexString(secret2));

            // Now we can test encryption
            EncryptionUtil encryptionUtil1 = new EncryptionUtil(secret1);
            String message = "Hello, I was successfully encrypted and decrypted";

            // Check encryption
            byte[] encryptedMessage = encryptionUtil1.encrypt(message.getBytes(StandardCharsets.UTF_8));
            assertNotEquals(message, new String(encryptedMessage));
            
            byte[] encodedEncryptionParams = encryptionUtil1.getEncodedParams();

            // A -> B

            // Check decryption
            EncryptionUtil encryptionUtil2 = new EncryptionUtil(secret2, encodedEncryptionParams);
            String decryptedMessage = new String(encryptionUtil2.decrypt(encryptedMessage));
            assertEquals(message, decryptedMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}