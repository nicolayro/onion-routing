package no.ntnu.onion.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class EncryptionUtil {
    private static final String SECRET_KEY_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private Cipher encryptionCipher;
    private Cipher decryptionCipher;

    public EncryptionUtil(byte[] secret) {
        try {
            SecretKeySpec aesKey = new SecretKeySpec(secret, 0, 16, SECRET_KEY_ALGORITHM);

            this.encryptionCipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            this.encryptionCipher.init(Cipher.ENCRYPT_MODE, aesKey);

            AlgorithmParameters aesParams = AlgorithmParameters.getInstance(SECRET_KEY_ALGORITHM);
            aesParams.init(getEncodedParams());

            this.decryptionCipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            this.decryptionCipher.init(Cipher.DECRYPT_MODE, aesKey, aesParams);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public EncryptionUtil(byte[] secret, byte[] encodedParams) {
        try {
            SecretKeySpec aesKey = new SecretKeySpec(secret, 0, 16, SECRET_KEY_ALGORITHM);

            AlgorithmParameters aesParams = AlgorithmParameters.getInstance(SECRET_KEY_ALGORITHM);
            aesParams.init(encodedParams);

            // Setting up for encryption with the given params
            this.encryptionCipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            this.encryptionCipher.init(Cipher.ENCRYPT_MODE, aesKey, aesParams);

            // Setting up for decryption with the given params
            this.decryptionCipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            this.decryptionCipher.init(Cipher.DECRYPT_MODE, aesKey, aesParams);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public byte[] getEncodedParams() {
        try {
            return encryptionCipher.getParameters().getEncoded();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public byte[] encrypt(byte[] message){
        try {
            return encryptionCipher.doFinal(message);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] decrypt(byte[] encryptedMessage){
        try {
           return decryptionCipher.doFinal(encryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
