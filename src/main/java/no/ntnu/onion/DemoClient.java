package no.ntnu.onion;

import no.ntnu.onion.routing.OnionRouter;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.Scanner;

/**
 * Used for demonstrating how the onion routing works.
 */
public class DemoClient {
    public static void main(String[] args) throws IOException, InvalidKeyException {
        int destinationPort = 1010;

        OnionRouter onionRouter = new OnionRouter(1010);

        // Little demo of a loop sending message
        Scanner in = new Scanner(System.in);
        String input;
        String response;
        System.out.println("Please write something:");
        while(!(input = in.nextLine()).equals("done")) {

            onionRouter.send(input); // Send the given message
            response = onionRouter.response(); // We wait for a response

            System.out.println(new String(response));
        }
    }
}


