package no.ntnu.onion.routing;

import no.ntnu.onion.util.Connection;
import no.ntnu.onion.util.DiffieHellman;
import no.ntnu.onion.util.EncryptionUtil;
import no.ntnu.onion.util.MessageUtil;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;


/**
 * This is the class which can be used for sending and receiving requests to a certain port, and the connection will
 * be routed through three nodes with layered encryption, following the concept of onion routing.
 *
 * The actual bytes send through the nodes has to have a certain structure for the connection to works,
 * basically its own protocol. The structure currently used can be found further down. Note that these can change at
 * any time.
 *
 *
 * ------ BYTE ARRAY STRUCTURE -----
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
 *       ----------------------------------------
 *      | 0 | To | MSGLN |   Message  | Padding |
 *      ----------------------------------------
 *                  ^
 *           Message length
 *
 */
public class OnionRouter {
    private static final int FIRST = 1001;
    private static final int SECOND = 1002;
    private static final int THIRD = 1003;
    private int destinationPort;

    private EncryptionUtil encryptionUtil1;
    private EncryptionUtil encryptionUtil2;
    private EncryptionUtil encryptionUtil3;

    private DiffieHellman keyExchange;
    private MessageUtil messageUtil;
    private Connection connection;


     /**
      * A connection is set up with the given destination port, and allows for onion routed requests between them
      *
      * This following code makes handshakes with all nodes the message wil pass through. It follows the following strategy
      * to reach all nodes:
      *     - (1) Handshake node 1
      *     - (2) Message node 1 with a message for node 2 containing handshake with node 2
      *     - (3) Message node 1 with a message for node 2 with a message for node 3 containing handshake with node 3
      *
      */
    public OnionRouter(int destinationPort) throws IOException, InvalidKeyException {
        // Setup
        this.destinationPort = destinationPort;
        this.keyExchange = new DiffieHellman();
        this.messageUtil = new MessageUtil();
        this.connection = new Connection(new Socket("localhost", FIRST));

        // (1)
        // We make the handshake with the first node
        byte[] ourPublicKey = keyExchange.initializeKeyPair();
        // Send our public key
        byte[] handshake = messageUtil.createHandshake(ourPublicKey);
        connection.send(handshake);

        // Now we wait for a response. This should contain their public key as well as the params they used
        byte[] response = connection.read();

        byte[] theirPublicKey = messageUtil.readPublicKey(response);
        byte[] theirParams = messageUtil.readParams(response);

        // We generate the secret, and we are ready to start encrypting and decrypting
        byte[] secret = keyExchange.generateSecret(keyExchange.instantiatePublicKey(theirPublicKey));
        encryptionUtil1 = new EncryptionUtil(secret, theirParams);

        // (2)
        byte[] handshakeMessage= messageUtil.createMessage(encryptionUtil1.encrypt(messageUtil.addWhereTo(messageUtil.createHandshake(ourPublicKey), SECOND)));
        connection.send(handshakeMessage);
        response = connection.read();
        byte[] decryptedResponse = encryptionUtil1.decrypt(response);
        theirPublicKey = messageUtil.readPublicKey(decryptedResponse);
        theirParams = messageUtil.readParams(decryptedResponse);
        secret = keyExchange.generateSecret(keyExchange.instantiatePublicKey(theirPublicKey));
        encryptionUtil2 = new EncryptionUtil(secret, theirParams);

        // (3)
        handshakeMessage = messageUtil.createMessage(encryptionUtil1.encrypt(messageUtil.addWhereTo(messageUtil.createMessage(encryptionUtil2.encrypt(messageUtil.addWhereTo(messageUtil.createHandshake(ourPublicKey), THIRD))), SECOND)));
        connection.send(handshakeMessage);
        response = connection.read();
        decryptedResponse = encryptionUtil2.decrypt(encryptionUtil1.decrypt(response));
        theirPublicKey = messageUtil.readPublicKey(decryptedResponse);
        theirParams = messageUtil.readParams(decryptedResponse);
        secret = keyExchange.generateSecret(keyExchange.instantiatePublicKey(theirPublicKey));
        encryptionUtil3 = new EncryptionUtil(secret, theirParams);

        // Handshake complete
    }


    /**
     * Sends the given message to the port in which this instance of the object is connection to, while routing trough
     * the nodes for onion routing.
     *
     * @param message message to send
     */
    public void send(String message) {
        byte[] oni = addLayer(message.getBytes(StandardCharsets.UTF_8), encryptionUtil3, destinationPort);
        byte[] onio = addLayer(oni, encryptionUtil2, THIRD);
        byte[] onion = addLayer(onio, encryptionUtil1, SECOND);
        connection.send(onion);
    }

    /**
     * After sending a message, a response can be expected. This returns this response.
     *
     * @return response of a request
     */
    public String response() {
        byte[] onion = connection.read();
        byte[] onio = peelLayer(onion, encryptionUtil1);
        byte[] oni = peelLayer(onio, encryptionUtil2);
        return new String(peelLayer(oni, encryptionUtil3));
    }

    /**
     * Helper method for the abstraction of "adding a layer". Currently this method takes in a message, where to send
     * it and the encryption spec needed for that location, and creates a byte array in accordance to the
     * structure defined at the top of this class.
     *
     * @param message message to add a layer to
     * @param encryptionUtil encryption spec
     * @param to location port, for example 1001
     * @return
     */
    private byte[] addLayer(byte[] message, EncryptionUtil encryptionUtil, int to) {
        return messageUtil.createMessage(encryptionUtil.encrypt(messageUtil.addWhereTo(message, to)));
    }

    /**
     * Helper method for the abstraction of "peeling a layer". Currently this method decrypts a messgae according to
     * the spec in the given EncryptionUtil
     *
     * @param message message to "peel" or decrypt
     * @param encryptionUtil encryption spec
     * @return decrypted message
     */
    private byte[] peelLayer(byte[] message, EncryptionUtil encryptionUtil) {
        return encryptionUtil.decrypt(message);
    }

}