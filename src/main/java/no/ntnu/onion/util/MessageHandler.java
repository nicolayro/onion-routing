package no.ntnu.onion.util;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Handles I/O between sockets
 */
public class MessageHandler {
    // Defines how many bytes are allocated for different data
    public static final int BYTES_FOR_MSG_LEN = 4;

    private final InputStream inputStream;
    private final OutputStream outputStream;

    /**
     * Creates I/O Streams between the current socket and the socket given in the constructor
     *
     * @param connection socket to connect with
     * @throws IOException if there is an error
     */
    public MessageHandler(Socket connection) throws IOException {
        inputStream = connection.getInputStream();
        outputStream = connection.getOutputStream();
    }

    /**
     * Reads a message given from the socket, and prints it to the terminal
     * Prints an error message if there is an error
     *
     * @return byte array that was read form
     */
    public byte[] readMessage() {
        try {
            byte[] messageLengthBytes = inputStream.readNBytes(BYTES_FOR_MSG_LEN);
            int messageLength = getInt(messageLengthBytes);
            byte[] message = new byte[messageLength];
            inputStream.readNBytes(message, 0 , messageLength);
            return message;
        } catch(Exception e) {
            System.out.println("Was not able to read message");
            return new byte[0];
        }
    }

    /**
     * Send a message to the given socket
     * Prints an error if there is an error
     *
     * @param message to send
     */
    public void sendMessage(byte[] message) {
        try {
            outputStream.write(addLengthToStart(message));
        } catch (Exception e) {
            System.out.println("Was not able to send message!");
        }
    }

    /**
     * Closes I/O streams. This instance is of the object is no longer usable after this call
     */
    public void close() {
        try {
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            System.out.println("There was an error closing the I/O streams");
        }
    }

    /**
     * This method takes a byte array and adds _four_ bytes to the beginning of the array to define the length
     * of the message
     *
     * @return original message, with the length of the message padded to the first four bytes of the message
     *
     */
    public byte[] addLengthToStart(byte[] arr) {
        byte[] newArr = new byte[arr.length + BYTES_FOR_MSG_LEN];
        byte[] lengthAsBytes = getBytes(arr.length);
        System.arraycopy(lengthAsBytes, 0, newArr, 0, lengthAsBytes.length);
        System.arraycopy(arr, 0, newArr, lengthAsBytes.length, arr.length);
        return newArr;
    }

    /**
     * Creates an int of four bytes
     *
     * @param arr array to convert
     * @return returns int
     */
    public int getInt(byte[] arr) {
        return ByteBuffer.wrap(arr).getInt();
    }

    /**
     * Creates a short of two bytes
     *
     * @param arr array to convert
     * @return returns short
     */
    public short getShort(byte[] arr) {
        return ByteBuffer.wrap(arr).getShort();
    }

    /**
     * Helper method for converting an integer into a four byte array
     *
     * @param value integer to be converted
     * @return array of four bytes, representing the integer value
     */
    public byte[] getBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }
}
