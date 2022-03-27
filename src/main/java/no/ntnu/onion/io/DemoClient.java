package no.ntnu.onion.io;

import java.io.IOException;
import java.security.InvalidKeyException;

/**
 * Used for demonstrating how the onion routing works.
 */
public class DemoClient {
    public static void main(String[] args) throws IOException, InvalidKeyException {
        OnionRouter demo = new OnionRouter();
        demo.start();
    }
}


