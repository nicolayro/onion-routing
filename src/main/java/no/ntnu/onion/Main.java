package no.ntnu.onion;

import no.ntnu.onion.network.Node;
import no.ntnu.onion.util.DiffieHellman;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java Main.java <port>");
        }
        try {
            Integer port = Integer.parseInt(args[0]);
            System.out.printf("Start server on port %d\n", port);
            new Node(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
