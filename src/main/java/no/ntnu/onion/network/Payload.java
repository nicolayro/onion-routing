package no.ntnu.onion.network;

/**
 * This class defines _decrypted_ the structure of a message in the onion routing system. This is an integral part of
 * the actual program. This structure should be translated to some sort of byte structure
 */
public class Payload {
    private String message; // Actual message trying to be sent. This is the actual "onion"
    private Integer to; // Node to further the message to
    private String publicKey; // Public key used in encryption of the message.

    /**
     * Empty constructor
     */
    public Payload(){}

    public Payload(String payload) {
        String[] data = payload.split(";");
        if(data.length == 1) {
            this.message = data[0];
        }
        if(data.length == 3) {
            this.message = data[0];
            this.to = Integer.parseInt(data[1]);
            this.publicKey = data[2];
        }

    }

    /*
    ------------------------------------------------------
    Accessor-methods
    ------------------------------------------------------
     */

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /*
    ------------------------------------------------------
    Other methods
    ------------------------------------------------------
    */

    /**
     * To formatted string is a make-shift deserialization method to create a string with a certain format for
     * onion routing
     *
     * The current format is:
     *      "<message> ; <to> ; <public-key>"
     *
     */
    public String toFormattedString() {
        return message + ";" + to + ";" + "publicKey";
    }

    @Override
    public String toString() {
        return "Payload{" +
                "message='" + message + '\'' +
                ", to=" + to +
                ", publicKey='" + publicKey + '\'' +
                '}';
    }
}
