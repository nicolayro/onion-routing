package no.ntnu.onion.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MessageUtil {
    public static final int HANDSHAKE = 0;
    public static final int MESSAGE = 1;

    private static final int BYTES_FOR_LEN = 4;

    /**
     * Empty constructor
     */
    public MessageUtil() {}

    /**
     * Creates a message with the byte structure defined for a handshake.
     * @param publicKey public key to send in handshake
     *
     * @return a complete handshake message, ready to be sent
     */
    public byte[] createHandshake(byte[] publicKey) {
        byte[] publicKeyWithLength = addLengthToStart(publicKey, BYTES_FOR_LEN);

        byte[] handshake = new byte[publicKeyWithLength.length + 1]; // + 1 For the handshake flagH
        handshake[0] = HANDSHAKE;
        System.arraycopy(publicKeyWithLength, 0, handshake, 1, publicKeyWithLength.length);

        return handshake;
    }

    public byte[] createMessage(byte[] message) {
        byte[] request = new byte[message.length + 1]; // + 1 For the handshake flagH
        request[0] = MESSAGE;
        System.arraycopy(message, 0, request, 1, message.length);
        return request;
    }

    /**
     * Combines to arrays of bytes with each other, each with a few bytes in before the actual data indication the
     * length of the data
     * @param arr1 first array
     * @param arr2 second array
     * @return the combined array with length defines before eachH
     */
    public byte[] combineArrays(byte[] arr1, byte[] arr2) {
        byte[] arr1WithLen = addLengthToStart(arr1, BYTES_FOR_LEN);
        byte[] arr2WithLen = addLengthToStart(arr2, BYTES_FOR_LEN);
        byte[] message = new byte[arr1WithLen.length + arr2WithLen.length];

        // Copy the values into the message
        System.arraycopy(arr1WithLen, 0, message, 0, arr1WithLen.length);
        System.arraycopy(arr2WithLen, 0, message, arr1WithLen.length, arr2WithLen.length);

        return message;
    }

    /**
     * Parses the defined byte structure for a public key message
     */
    public byte[] readPublicKey(byte[] arr) {
        return readBytes(arr, BYTES_FOR_LEN);
    }

    /**
     * Parses the params from a handshake response with a public key and params. This method expects the full response
     * and not just the public key
     */
    public byte[] readParams(byte[] arr) {
        byte[] publicKey = readPublicKey(arr);
        byte[] paramsWithLen = new byte[arr.length - publicKey.length - BYTES_FOR_LEN];
        System.arraycopy(arr, publicKey.length + BYTES_FOR_LEN, paramsWithLen, 0, paramsWithLen.length);
        return readBytes(paramsWithLen, BYTES_FOR_LEN);
    }

    /**
     * Reads a byte array where the beginning of the array defines the length of the actual data.
     *
     * @param arr array to read from
     * @param bytesForLen amount of bytes allocated for the length of the data
     * @return the data
     */
    private byte[] readBytes(byte[] arr, int bytesForLen) {
        byte[] len = new byte[bytesForLen];
        System.arraycopy(arr, 0, len, 0, bytesForLen);
        byte[] data = new byte[getInt(len)];
        System.arraycopy(arr, bytesForLen, data, 0, data.length);
        return data;
    }

    /**
     * Creates an int of four bytes
     *
     * @param arr array to convert
     * @return returns int
     */
    private int getInt(byte[] arr) {
        return ByteBuffer.wrap(arr).getInt();
    }

    /**
     * Creates a short of two bytes
     *
     * @param arr array to convert
     * @return returns short
     */
    private short getShort(byte[] arr) {
        return ByteBuffer.wrap(arr).getShort();
    }

    /**
     * Helper method for converting an integer into a four byte array
     *
     * @param value integer to be converted
     * @return array of four bytes, representing the integer value
     */
    private byte[] getBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    /**
     * Removes first byte from an array of bytes
     * @param arr array to remove from
     *
     * @return new array without the first byte
     */
    public byte[] trimFirst(byte[] arr) {
        byte[] trimmedArr = new byte[arr.length - 1];
        System.arraycopy(arr, 1, trimmedArr, 0, trimmedArr.length);
        return trimmedArr;
    }

    /**
     * This method takes a byte array and adds the given amount of bytes to the beginning of the array to
     * define the length of the message
     *
     * @param arr array to add to
     * @param bytesForLen how many bytes to allocate for defining the length of the data
     * @return original message, with the length of the message padded to the first four bytes of the message
     */
    public byte[] addLengthToStart(byte[] arr, int bytesForLen) {
        byte[] newArr = new byte[arr.length + bytesForLen];
        byte[] lengthAsBytes = getBytes(arr.length);
        System.arraycopy(lengthAsBytes, 0, newArr, 0, lengthAsBytes.length);
        System.arraycopy(arr, 0, newArr, lengthAsBytes.length, arr.length);
        return newArr;
    }

    /**
     * Returns the short value of the first two bytes
     * @param arr arr to read
     * @return short value of the first to bytes
     */
    public short readWhereTo(byte[] arr) {
        return getShort(new byte[]{arr[0], arr[1]});
    }
}
