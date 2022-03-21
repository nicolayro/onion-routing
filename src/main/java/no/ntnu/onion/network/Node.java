package no.ntnu.onion.network;

import no.ntnu.onion.util.MessageHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class represents the nodes used in the onion routing implementation. A node can both send and receive messages, as
 * well as encrypt and decrypt them.
 *
 * In practice the Node can act as either party in a client/server-connection
 */
public class Node {
    private final String privateKey;
    private final Integer port;
    private final ServerSocket serverSocket;


    /**
     * Creates a new node, bind to the given port
     * @param port port to use for the connection
     */
    public Node(Integer port) throws IOException {
        this.port= port;
        this.privateKey = generatePrivateKey();

        if(port == 1000) {
            Socket connection = new Socket("", port + 1);
            MessageHandler clientTest = new MessageHandler(connection);
            clientTest.sendMessage(String.format("yo;%d;secret-key", port + 2));
            System.out.println("Answer: " + clientTest.readMessage());
        }

        // Handle server
        serverSocket = new ServerSocket(port);
        while(true) {
            MessageHandler messageHandler = new MessageHandler(serverSocket.accept());

            // We handle the receiving message, and get some sort of response
            String message = messageHandler.readMessage();
            String response = handleMessage(message);
            messageHandler.sendMessage(response);
        }
    }

    /**
     * When a Node receives a message, that message should be handled in the following way:
     *      - Decrypt message
     *      - Check who to send the message to
     *          - If there is no one, answer the message
     *          - If there is, send the message
     *      - At some point there should be a response from the message
     *          - This response should then be encrypted and should be sent back as an answer to the
     *            sender of the first message
     *
     * @param message to handle
     * @return answer
     */
    public String handleMessage(String message) {
        try {
            Payload payload = new Payload(message);
            // TODO: Decrypt message
            if(payload.getTo() == null) {
                return "Answer from " + port;
            }

            Socket connection = new Socket("", payload.getTo());
            MessageHandler messageHandler = new MessageHandler(connection);
            messageHandler.sendMessage(payload.getMessage());

            // At some later the point, the receiver of this message should have give some sort of answer

            // TODO: Add a timeout for this waiting

            // TODO: Encrypt response

            return messageHandler.readMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Removes a layer of encryption
     */
    private void peelLayer() {
        // Use session key to decrypt message
    }

    /**
     * Adds a layer of encryption
     */
    private void addLayer() {
    }

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!
     * THIS METHOD IS NOT MEANT TO BE USED IN IT'S CURRENT STATE AND IS ONLY LIKE THIS FOR
     * TESTING PURPOSES
     * !!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     * Generates a private key for the node
     *
     * @return key in string format
     */
    private String generatePrivateKey() {
        return "some random key for node on port " + port;
    }
}
