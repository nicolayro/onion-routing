import no.ntnu.onion.util.MessageUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageUtilTest {
    static MessageUtil messageUtil;

    @BeforeAll
    public static void initialize() {
        messageUtil = new MessageUtil();
    }

    @Test
    @DisplayName("Add handshake flag")
    public void createHandshake() {
        byte[] arr = {1};
        arr = messageUtil.createHandshake(arr);
        assertEquals(MessageUtil.HANDSHAKE, arr[0]);
    }

    @Test
    @DisplayName("Add message flag")
    public void createMessage() {
        byte[] arr = new byte[]{0};
        arr = messageUtil.createHandshake(arr);
        assertEquals(MessageUtil.MESSAGE, arr[1]);
    }

    @Test
    @DisplayName("Combine arrays")
    public void combineArrays() {
        byte[] arr1 = {0, 1, 2, 3};
        byte[] arr2 = {4, 5, 6, 7};
        byte[] arr3 = messageUtil.combineArrays(arr1, arr2);

        // Check that the length of the second array is defined as 4
        assertEquals(4, arr3[11]);
    }

    @Test
    @DisplayName("Trim first byte")
    public void trimFirst() {
        byte[] arr = {1, 2, 3, 4};
        arr = messageUtil.trimFirst(arr);
        assertEquals(2, arr[0]);
    }

    @Test
    @DisplayName("Add length of byte array as bytes to the beginning of the array")
    public void addLengthToStart() {
        byte[] arr = {5, 6, 7, 8};
        arr = messageUtil.addLengthToStart(arr, 4);
        assertEquals(4, arr[3]);
    }

    @Test
    @DisplayName("Add destination and length of message in front of message")
    public void addWhereTo() {
        byte[] arr = {1, 2, 3};
        arr = messageUtil.addWhereTo(arr, 1);
        assertEquals(1, arr[3]);
        assertEquals(3, arr[7]);
    }

}