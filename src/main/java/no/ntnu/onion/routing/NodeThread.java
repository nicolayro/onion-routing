package no.ntnu.onion.routing;

import no.ntnu.onion.util.DiffieHellman;
import no.ntnu.onion.util.EncryptionUtil;
import no.ntnu.onion.util.Connection;
import no.ntnu.onion.util.MessageUtil;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
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
public class NodeThread implements Runnable {
    private final Socket socket;
    private final Connection connection;
    private final MessageUtil messageUtil;
    private final DiffieHellman keyExchange;

    private EncryptionUtil encryptionUtil;
    private Connection nextConnection;

    public NodeThread(Socket socket) throws IOException {
        this.socket = socket;
        this.connection = new Connection(socket);
        this.keyExchange = new DiffieHellman();
        this.messageUtil = new MessageUtil();
    }

    @Override
    public void run() {
        // Little demo server
        if(socket.getLocalPort() == 1010) {
            while(true) {
                String request = new String(connection.read());
                System.out.println(request);
                byte[] message = ("Hello, Client! This is the Server. Your message was: " + request).getBytes(StandardCharsets.UTF_8);
                connection.send(message);
            }
        }

        try {
            byte[] message;
            byte[] answer;

            // The actual I/O loop
            while((message = connection.read()) != null) {
                // Trim request
                int requestType = message[0];
                byte[] requestPayload = messageUtil.trimFirst(message);

                if(requestType == MessageUtil.HANDSHAKE) {
                    // Handshake
                    answer = handleHandshake(requestPayload);
                } else {
                    // Message
                    answer = handleRequest(requestPayload);
                }

                // Answer
                connection.send(answer);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] handleHandshake(byte[] message){
        try {
            // Read their public key and instantiate it
            PublicKey theirPublicKey = keyExchange.instantiatePublicKey(messageUtil.readPublicKey(message));

            // Initialize our own keypair with the same parameter spec as their public key. We
            // also fetch our own public key for later use
            byte[] ourPublicKeyArr = keyExchange.initializeKeyPair(theirPublicKey);

            // Now we can generate the session key for encryption, again with their public key
            encryptionUtil = new EncryptionUtil(keyExchange.generateSecret(theirPublicKey));

            // We send back our public key and the params used for encryption
            return messageUtil.combineArrays(ourPublicKeyArr, encryptionUtil.getEncodedParams());
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] handleRequest(byte[] encryptedRequest) throws IOException {
        // The message at hand is now encrypted, according to the byte structure that is defined. Therefor we
        // start by decrypting it
        byte[] decryptedRequest = encryptionUtil.decrypt(encryptedRequest);

        int to = messageUtil.readWhereTo(decryptedRequest);
        byte[] message = messageUtil.readMessage(decryptedRequest);

        // Now we try to open a connection with the person to send to
        // At some point we should get a response. This response we should encrypt
        if(nextConnection == null) {
            nextConnection = new Connection(new Socket("localhost", to));
        }
        nextConnection.send(message);

        byte[] response = nextConnection.read();

        return encryptionUtil.encrypt(response);
    }


}
