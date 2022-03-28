package no.ntnu.onion.util;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

/**
 * Includes methods for handling key exchanges, using the Diffie-Hellman key exchange.
 */
public class DiffieHellman {
    private static final String KEYPAIR_ALGORITHM = "DH";
    private static final int KEYPAIR_SIZE = 2048;

    private KeyAgreement keyAgreement;

    /**
     * Empty constructor
     */
    public DiffieHellman(){}

    /**
     * Initializes key pair for this instance. This includes a private and a public key. The information is stored in
     * the instance, and the public key is return from the function for distribution
     *
     * @return the generated public key
     */
    public byte[] initializeKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEYPAIR_ALGORITHM);
            keyPairGenerator.initialize(KEYPAIR_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            this.keyAgreement = KeyAgreement.getInstance(KEYPAIR_ALGORITHM);
            keyAgreement.init(keyPair.getPrivate());

            // We encode the key
            return keyPair.getPublic().getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This initializes a key pair, but with the same parameters as the public key that is passed in. This is important
     * if one is to end up with the same key in a key exchange. The methods return are own public key for further distribution
     * @param publicKey public key from the other side of the exchange
     *
     * @return our own public key
     */
    public byte[] initializeKeyPair(PublicKey publicKey){
        // When the second keypair is initialized, it is important that it uses the same params as the first public key
        try {
            DHParameterSpec dhParameterSpec = ((DHPublicKey) publicKey).getParams();
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEYPAIR_ALGORITHM);
            keyPairGenerator.initialize(dhParameterSpec);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            this.keyAgreement = KeyAgreement.getInstance(KEYPAIR_ALGORITHM);
            keyAgreement.init(keyPair.getPrivate());

            return keyPair.getPublic().getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method creates a PublicKey object from an encoded public key
     *
     * @param encodedPublicKey encoded public key
     * @return public key as PublicKey object
     */
    public PublicKey instantiatePublicKey(byte[] encodedPublicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEYPAIR_ALGORITHM);
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encodedPublicKey);
            return keyFactory.generatePublic(x509EncodedKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates the secret used for encryption/decryption based on this instance's private key and the given public key
     *
     * @param publicKey public key to use for generation
     * @return the secret used for encryption/decryption
     * @throws InvalidKeyException thrown if the provided public key is invalid
     */
    public byte[] generateSecret(PublicKey publicKey) throws InvalidKeyException {
        keyAgreement.doPhase(publicKey, true);
        return keyAgreement.generateSecret();
    }


    /**
     * Converts a byte to hex digit and writes to the supplied buffer
     *
     * @param b byte
     * @param buf buf
     */
    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    /**
     * Converts a byte array to hex string
     *
     * @param block block to convert
     */
    public static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len-1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }

}
