package no.ntnu.onion.demo;

import no.ntnu.onion.util.ConnectionUtil;
import no.ntnu.onion.util.DiffieHellman;
import no.ntnu.onion.util.EncryptionUtil;
import no.ntnu.onion.util.MessageUtil;

import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;

/**
 * Used for demonstrating how the onion routing works.
 */
public class DemoClient {
    public static void main(String[] args) throws IOException {
        Demo demo = new Demo();
        demo.start();
    }
}


