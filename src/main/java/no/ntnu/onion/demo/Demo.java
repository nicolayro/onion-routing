package no.ntnu.onion.demo;

import no.ntnu.onion.routing.Node;
import no.ntnu.onion.util.ConnectionUtil;
import no.ntnu.onion.util.DiffieHellman;
import no.ntnu.onion.util.EncryptionUtil;
import no.ntnu.onion.util.MessageUtil;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Scanner;

public class Demo {

    private DiffieHellman keyExchange;
    MessageUtil messageUtil;
    ConnectionUtil connectionUtil;
    EncryptionUtil encryptionUtil;

    public void start() throws IOException {
        keyExchange = new DiffieHellman();
        messageUtil = new MessageUtil();
        connectionUtil = new ConnectionUtil(new Socket("localhost", 1000));

        // Imagine we want to send a message to port 1000
        // With the onion routing, we want this message to go through 3 different points before this port
        // Now this next method simulates a distributor server assigning 3 nodes to us in this list
        Node[] nodes;

        /*
        ----- HANDSHAKE -----
         */

        handshake();

        /*
        ----- MESSAGES -----
         */

        messaging();

    }

    private void handshake() {
        try {
            // Generate keypair
            byte[] ourPublicKey = keyExchange.initializeKeyPair();

            // Send our public key
            byte[] handshake = messageUtil.createHandshake(ourPublicKey);
            connectionUtil.send(handshake);

            // Now we wait for a response. This should contain their public key as well as the params they used
            byte[] response = connectionUtil.read();

            byte[] theirPublicKey = messageUtil.readPublicKey(response);
            byte[] theirParams = messageUtil.readParams(response);

            // We generate the secret, and we are ready to start encrypting and decrypting
            byte[] secret = keyExchange.generateSecret(keyExchange.instantiatePublicKey(theirPublicKey));
            encryptionUtil = new EncryptionUtil(secret, theirParams);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private void messaging() {
        Scanner in = new Scanner(System.in);
        String input;
        System.out.println("Please enter a message:");
        while (!(input = in.nextLine()).equals("done")) {
            byte[] encryptedMessage = encryptionUtil.encrypt(input.getBytes(StandardCharsets.UTF_8));

            connectionUtil.send(messageUtil.createMessage(encryptedMessage));
            byte[] response = connectionUtil.read();
            System.out.printf("Response from server: %s\n", new String(encryptionUtil.decrypt(response)));
        }
    }
}
