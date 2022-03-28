package no.ntnu.onion.routing;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * A node is an integral part to the onion routing concept. When a request is sent, it can be passed through these nodes.
 * A node can both send and receive messages, and then a request is received, a new thread is spawned for this request.
 *
 */
public class Node {
    private final Integer port;

    public Node(Integer port) throws IOException {
        this.port = port;
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
