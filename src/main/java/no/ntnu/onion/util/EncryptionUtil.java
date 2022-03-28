package no.ntnu.onion.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.AlgorithmParameters;

/**
 * Class for handeling encryption and decryption after a successful key exchange
 */
public class EncryptionUtil {
    private static final String SECRET_KEY_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private Cipher encryptionCipher;
    private Cipher decryptionCipher;

    /**
     * After a secret is created from a successful key exchange, it can be passed in this constructor, and the
     * instance will be ready to encrypt and decrypt messages with this secret. This constructor should only
     * be used by the _first_ party creating this class. The other one should also pas in the parameters that were used
     * which can be accessed with the #getEncodedParams method. They should then pass these in with the other constructor
     *
     * @param secret generated secret from a key exchange
     */
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

    /**
     * After a secret is created from a successful key exchange, it can be passed in this constructor, and the
     * instance will be ready to encrypt and decrypt with. Note that this method should be used by the _second_ party
     * that generates their instance, afther they have received their parameters from the other part of the exchange,
     * to ensure that the same paramters are used.
     *
     * @param secret generated secret after successful key exchange
     * @param encodedParams parameters used for other the other EncryptionUtil creation
     */
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

    /**
     * Returns the params used for instantiating this instance, encoded into bytes
     *
     * @return params used as bytes
     */
    public byte[] getEncodedParams() {
        try {
            return encryptionCipher.getParameters().getEncoded();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * Encrypts the given message with this instances secret
     *
     * @param message message to encrypt
     * @return encrypted message
     */
    public byte[] encrypt(byte[] message){
        try {
            return encryptionCipher.doFinal(message);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decrypts the given message with this instances secret
     *
     * @param encryptedMessage message to decrypt
     * @return decrypted message
     */
    public byte[] decrypt(byte[] encryptedMessage){
        try {
           return decryptionCipher.doFinal(encryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
