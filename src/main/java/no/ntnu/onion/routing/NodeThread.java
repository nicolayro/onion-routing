package no.ntnu.onion.routing;

import no.ntnu.onion.util.DiffieHellman;
import no.ntnu.onion.util.EncryptionUtil;
import no.ntnu.onion.util.MessageHandler;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

/**
 * When a client connects to a node, a new thread is spawned for handling that request. This thread
 * can receive, read and forward messages. The structure of these messages are defined below
 *
 * ------ BYTE ARRAY STRUCTURE -----
 *
 *
 *  The first byte defines if the message is a handshake or a message. What comes after depends on this. Here
 *  is a simple illustration:
 *
 *  Handshake (1):
 *
 *       Public Key Length
 *             v
 *   ----------------------------------------
 *  | 1 | PKLN |    Public key    | Padding |
 *  ----------------------------------------
 *
 *
 *  Message (0):
 *      Before decryption:
 *       ----------------------------------------
 *      | 0 |         Encrypted message         |
 *      ----------------------------------------
 *
 *      After decryption:
 *        ---------------------------------------
 *      | 0 | To | MSGLN |   Message  | Padding |
 *      ----------------------------------------
 *                  ^
 *           Message length
 *
 */
public class NodeThread implements Runnable{
    private static final int BYTES_FOR_PK_LEN = 4;

    private final Socket socket;
    private final DiffieHellman keyExchange;
    private MessageHandler messageHandler;
    private EncryptionUtil encryptionUtil;

    private int offset = 0;

    public NodeThread(Socket socket) {
        this.socket = socket;
        this.keyExchange = new DiffieHellman();
    }

    @Override
    public void run() {
        try {
            messageHandler = new MessageHandler(socket);
            byte[] request;
            byte[] response;

            // The actual I/O loop
            while((request = messageHandler.readMessage()) != null) {
                response = handleRequest(request);
                messageHandler.sendMessage(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] handleRequest(byte[] request) {
        if(request[0] == 1) {
            offset++;
            System.out.println("Handshake");
            return handleHandshake(request);
            // The request is a handshake! We have to read the public key, and use it for initializing our own.
        }
        System.out.println("Message");
        handleMessage(request);
        byte[] response = "Thank you for the request, here is the response".getBytes(StandardCharsets.UTF_8);
        return encryptionUtil.encrypt(response);
    }

    private byte[] handleHandshake(byte[] request) {
        // Read the first to bytes for the length of the public key
        byte[] publicKeyLengthArr = new byte[BYTES_FOR_PK_LEN];
        System.arraycopy(request, offset, publicKeyLengthArr, 0, publicKeyLengthArr.length);
        int publicKeyLength = messageHandler.getInt(publicKeyLengthArr);
        System.out.printf("Public key length: %d\n", publicKeyLength);
        offset += publicKeyLengthArr.length;

        // Read the actual public key
        byte[] publicKeyArr = new byte[publicKeyLength];
        System.arraycopy(request, offset, publicKeyArr, 0, publicKeyArr.length);

        // Now we can generate our own keypair
        PublicKey publicKey = keyExchange.instantiatePublicKey(publicKeyArr);
        byte[] ourPublicKey =  keyExchange.initializeKeyPair(publicKey);
        byte[] ourPublicKeyWithLength = messageHandler.addLengthToStart(ourPublicKey);

        byte[] response = new byte[0];

        // Now we can generate the secret
        try {
            offset = 1;
            byte[] secret = keyExchange.generateSecret(publicKey);
            System.out.println("Secret Server-Side: " + DiffieHellman.toHexString(secret));
            encryptionUtil = new EncryptionUtil(secret);

            byte[] encryptionParams = encryptionUtil.getEncodedParams();
            byte[] encryptionParamsWithLength = messageHandler.addLengthToStart(encryptionParams);

            response = new byte[ourPublicKeyWithLength.length + encryptionParamsWithLength.length];
            System.arraycopy(ourPublicKeyWithLength, 0, response, 0, ourPublicKeyWithLength.length);
            System.arraycopy(encryptionParamsWithLength, 0, response, ourPublicKeyWithLength.length, encryptionParamsWithLength.length);

            // If we get here, we are ready to encrypt and decrypt

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        // Now that everything has gone well, we must return our own public key such that the sender can of
        // the other key can create the same secret we just created
        return response;

    }

    private void handleMessage(byte[] request) {

        byte[] message = new byte[request.length - 1];
        System.arraycopy(request, 1, message, 0, message.length);
        System.out.println("Request: " + encryptionUtil.decrypt(message));

    }


}
