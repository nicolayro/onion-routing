package no.ntnu.onion.routing;

import no.ntnu.onion.util.DiffieHellman;
import no.ntnu.onion.util.MessageHandler;

import java.net.Socket;

public class NodeThread implements Runnable{
    private Socket socket;
    private DiffieHellman keyExchange;

    public NodeThread(Socket socket) {
        this.socket = socket;
        this.keyExchange = new DiffieHellman();
    }

    @Override
    public void run() {
        try {
            System.out.println("Starting new thread on port " + socket.getPort());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
