package no.ntnu.onion.io;

import no.ntnu.onion.util.Connection;
import no.ntnu.onion.util.DiffieHellman;
import no.ntnu.onion.util.EncryptionUtil;
import no.ntnu.onion.util.MessageUtil;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Scanner;

public class OnionRouter {

    private static final int FIRST = 1003;
    private static final int SECOND = 1001;
    private static final int THIRD = 1002;

    int destinationPort = 1010;

    private DiffieHellman keyExchange;
    private MessageUtil messageUtil;
    private Connection connection;

    public void start() throws IOException, InvalidKeyException {

        keyExchange = new DiffieHellman();
        messageUtil = new MessageUtil();
        connection = new Connection(new Socket("localhost", FIRST));

        /*
         This following code makes handshakes with all nodes the message wil pass through. It follows the following strategy
         to reach all nodes:
              - (1) Handshake node 1
              - (2) Message node 1 with a message for node 2 containing handshake with node 2
              - (3) Message node 1 with a message for node 2 with a message for node 3 containing handshake with node 3
         */

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
        EncryptionUtil encryptionUtil1 = new EncryptionUtil(secret, theirParams);

        // (2)
        byte[] handshakeMessage= messageUtil.createMessage(encryptionUtil1.encrypt(messageUtil.addWhereTo(messageUtil.createHandshake(ourPublicKey), SECOND)));
        connection.send(handshakeMessage);
        response = connection.read();
        byte[] decryptedResponse = encryptionUtil1.decrypt(response);
        theirPublicKey = messageUtil.readPublicKey(decryptedResponse);
        theirParams = messageUtil.readParams(decryptedResponse);
        secret = keyExchange.generateSecret(keyExchange.instantiatePublicKey(theirPublicKey));
        EncryptionUtil encryptionUtil2 = new EncryptionUtil(secret, theirParams);

        // (3)
        handshakeMessage = messageUtil.createMessage(encryptionUtil1.encrypt(messageUtil.addWhereTo(messageUtil.createMessage(encryptionUtil2.encrypt(messageUtil.addWhereTo(messageUtil.createHandshake(ourPublicKey), THIRD))), SECOND)));
        connection.send(handshakeMessage);
        response = connection.read();
        decryptedResponse = encryptionUtil2.decrypt(encryptionUtil1.decrypt(response));
        theirPublicKey = messageUtil.readPublicKey(decryptedResponse);
        theirParams = messageUtil.readParams(decryptedResponse);
        secret = keyExchange.generateSecret(keyExchange.instantiatePublicKey(theirPublicKey));
        EncryptionUtil encryptionUtil3 = new EncryptionUtil(secret, theirParams);


        // Now we can start sending messages
        Scanner in = new Scanner(System.in);
        String input;
        System.out.println("Please write something");
        while(!(input = in.nextLine()).equals("done")) {

            // Building the onion
            byte[] on = addLayer(input.getBytes(StandardCharsets.UTF_8), encryptionUtil3, destinationPort);
            byte[] oni = addLayer(on, encryptionUtil2, THIRD);
            byte[] onion = addLayer(oni, encryptionUtil1, SECOND);
            connection.send(onion);

            // We wait for the response and destruct the onion
            onion = connection.read();
            oni = peelLayer(onion, encryptionUtil1);
            on = peelLayer(oni, encryptionUtil2);
            response = peelLayer(on, encryptionUtil3);

            System.out.println(new String(response));
        }

    }

    private byte[] addLayer(byte[] message, EncryptionUtil encryptionUtil, int to) {
        return messageUtil.createMessage(encryptionUtil.encrypt(messageUtil.addWhereTo(message, to)));
    }

    private byte[] peelLayer(byte[] message, EncryptionUtil encryptionUtil) {
        return encryptionUtil.decrypt(message);
    }

}
