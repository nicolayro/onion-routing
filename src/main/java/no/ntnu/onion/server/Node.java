package no.ntnu.onion.server;

/**
 * This class represents the nodes used in the onion routing implementation. A node can send and receive messages, as
 * well as encrypt and decrypt them.
 */
public class Node {
    private Integer port;

    /**
     * Creates a new node, bind to the given port
     * @param port port to use for the connection
     */
    public Node(Integer port) {
        this.port= port;
    }


}
