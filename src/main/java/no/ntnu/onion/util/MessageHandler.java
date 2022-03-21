package no.ntnu.onion.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles I/O between sockets
 */
public class MessageHandler {
    private final BufferedReader reader;
    private final PrintWriter writer;

    /**
     * Creates I/O Streams between the current socket and the socket given in the constructor
     *
     * @param connection socket to connect with
     * @throws IOException if there is an error
     */
    public MessageHandler(Socket connection) throws IOException {
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        writer = new PrintWriter(connection.getOutputStream(), true);
    }

    /**
     * Reads a message given from the socket, and prints it to the terminal
     * Prints an error message if there is an error
     *
     * @return message in string form
     */
    public String readMessage() {
        try {
            return reader.readLine();
        } catch(Exception e) {
            System.out.println("Was not able to read message");
            return "There was an error";
        }
    }

    /**
     * Send a message to the given socket
     * Prints an error if there is an error
     *
     * @param message to send
     */
    public void sendMessage(String message) {
        try {
            writer.println(message);
        } catch (Exception e) {
            System.out.println("Was not able to send message!");
        }
    }

    /**
     * Sends an object to the given socket
     *
     * @param object to send
     */
    public void sendObject(Object object) {
        try {
            writer.print(object);
        } catch (Exception e) {
            System.out.println("Was not able to send object!");
        }
    }

    /**
     * Closes I/O streams. This instance is of the object is no longer usable after this call
     */
    public void close() {
        try {
            reader.close();
            writer.close();
        } catch (Exception e) {
            System.out.println("There was an error closing the I/O streams");
        }
    }
}
