package no.ntnu.onion.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class EncryptionUtil {
    private static final String SECRET_KEY_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private byte[] secret;
    private Cipher cipher;

    public EncryptionUtil(byte[] secret) {
        this.secret= secret;
        try {
            SecretKeySpec aesKey = new SecretKeySpec(secret, 0, 16, SECRET_KEY_ALGORITHM);
            this.cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            this.cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }

    }

    public byte[] getEncodedParams() {
        try {
            return cipher.getParameters().getEncoded();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public byte[] encrypt(String message) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {
        byte[] cleartext = message.getBytes();
        return cipher.doFinal(cleartext);
    }

    public String decrypt(byte[] encryptedMessage, byte[] encodedParams) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec aesKey = new SecretKeySpec(secret, 0, 16, SECRET_KEY_ALGORITHM);
        AlgorithmParameters aesParams = AlgorithmParameters.getInstance(SECRET_KEY_ALGORITHM);
        aesParams.init(encodedParams);
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, aesParams);
        return new String(cipher.doFinal(encryptedMessage));
    }
}
