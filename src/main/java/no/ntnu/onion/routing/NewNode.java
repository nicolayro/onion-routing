package no.ntnu.onion.routing;

import no.ntnu.onion.util.DiffieHellman;

import java.io.IOException;
import java.net.ServerSocket;

public class NewNode {
    private Integer port;

    public NewNode(Integer port) throws IOException {
        this.port = port;
        startServer();
    }


    public void startServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
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
