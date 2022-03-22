package no.ntnu.onion.routing;

import no.ntnu.onion.util.DiffieHellman;
import no.ntnu.onion.util.EncryptionUtil;
import no.ntnu.onion.util.MessageHandler;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.PublicKey;

public class Node {
    private final Integer port;

    public Node(Integer port) throws IOException {
        this.port = port;
        if(port == 1000 | port == 1002) {
            EncryptionUtil encryptionUtil;
            MessageHandler messageHandler = new MessageHandler(new Socket("localhost", 1001));

            int offset = 0;
            byte messageFlag = 1;

            DiffieHellman diffieHellman = new DiffieHellman();
            byte[] publicKey = diffieHellman.initializeKeyPair();

            byte[] publicKeyWithLength = messageHandler.addLengthToStart(publicKey);

            byte[] message = new byte[publicKeyWithLength.length + 1];
            message[0] = messageFlag;
            offset++;
            System.arraycopy(publicKeyWithLength, 0, message , offset, publicKeyWithLength.length);
            messageHandler.sendMessage(message);

            byte[] response= messageHandler.readMessage();
            int pkLen = messageHandler.getInt(new byte[]{response[0], response[1], response[2], response[3]});
            byte[] otherPublicKeyArr = new byte[pkLen];
            offset = pkLen + 4;

            int paramLen = messageHandler.getInt(new byte[]{
                    response[offset],
                    response[offset + 1],
                    response[offset + 2],
                    response[offset + 3]
            });

            byte[] params = new byte[paramLen];

            System.arraycopy(response, 4, otherPublicKeyArr, 0, pkLen);

            System.arraycopy(response, offset + 4, params, 0, paramLen);

            // Using the other publicKey to create secret
            PublicKey otherPublicKey = diffieHellman.instantiatePublicKey(otherPublicKeyArr);
            try {
                byte[] secret = diffieHellman.generateSecret(otherPublicKey);
                System.out.println("Secret Client-side: " + DiffieHellman.toHexString(secret));
                encryptionUtil = new EncryptionUtil(secret, params);

                // If we get here, we are ready to encrypt

                // Example message
                byte[] newMessage= "Hello, this message should be encrypted".getBytes(StandardCharsets.UTF_8);
                byte[] encryptedMessage = encryptionUtil.encrypt(newMessage);
                message = new byte[encryptedMessage.length + 1];
                message[0] = 0;
                System.arraycopy(encryptedMessage, 0, message, 1, encryptedMessage.length);

                messageHandler.sendMessage(message);
                byte[] answer = messageHandler.readMessage();
                System.out.println("Response: " + encryptionUtil.decrypt(answer));
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }

        }
        startServer(new ServerSocket(port));
    }

    /**
     * Starts a listening for connections. When a client tries to connect, a new thread is spawned for that connection
     * allowing for multiple connection
     * @param serverSocket Server socket to use as a base for connection
     */
    public void startServer(ServerSocket serverSocket) {
        while(true) {
            try {
                Thread t = new Thread(new NodeThread(serverSocket.accept()));
                t.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
