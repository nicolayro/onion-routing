
# Anonyon - An Onion Routing Library

## Intro

This project is an assignment for IDATT2104 - Nettverksprogrammering.
The assignment is to implement **onion routing** either as software or as a library. 
We are free to use any programming language, but C++ or Rust implementations are more desired. Even though I would really like to learn more C++, given the complexity of the task I chose to use Java, as it is what I'm most comfortable with.

## Implemented functionality
- A OnionRouter class, capable of sending and receiving requests to a server with routing through three nodes with layered encryption between them. To achieve this, the following functionality is implemented:
  - Nodes that can receive and send traffic
  - Key generation and safe key exchange for encryption
  - Construction and interpretation of bytes streamed between endpoints.
- A simple Demo Client and Sever, for testing the functionality of the Onion Router.

## Plans

The task does not require completion of all the listed functionality, and because of the size of the task, prioritizing certain pieces of functionality is desired. Therefor,
I have created a list of goals I can strive for. The point is not to complete all the goals, but to give a path to follow and give an indication of the priority of features.

### Goals:
	- [x] Setup client and server
	- [x] Encrypt normal messages between them
	- [x] Create node than can add and peel layers from the message
	- [x] Route through 3 nodes such that the route is anonymus
    - [x] Create an actual protocol-like, fixed-size bytestructure.
	- [] Automatically distribute nodes when a request is made
    - [] Continius integration/deployment

### Weaknesses

As with any program, there are multiple known weaknesses:

    - Message are not of equal length, meaning that if one were to follow a message from start to end you could see it shrink as the "layers are peeled"
    - No distributer node, the nodes used are currently hard coded in the program for demonstration purposes
    - More error handling with proper error messages for the user
    - Still some work needed to make it a functional library for existing 

## External Libraries

This project has yet to make use of any external libraries

## Installation Guide

To get started, you can clone the repo from the command line:
> git clone https://github.com/nicolayro/onion-routing

After the project is successfully cloned, go into it:
> cd onion-routing

Ready the project:
> mvn clean install

After this the program should be ready to go. See the Usage Guide on how to start using the program!

## Usage Guide

The onion routing currently routes through the nodes on three local ports on your computer. As for now these ports are 
hardcoded in as:

- 1001
- 1002
- 1003

Therefore, an instance of the program should be run on these three ports "in the background". To set this up, open three terminal windows, run this command in the root directory of the project:
`java -jar target/onion-1.0.jar <port>`. So, for example for port 1001 you would run:
> java -jar target/onion-1.0.jar 1001

_Note: if one wishes to change these hardcoded ports, it is possible by simply changing the static constants defined at the top of the OnionRouter class. These are known as *FIRST*, *SECOND* and *THIRD*, also indicating the order in which the nodes are routed through._

Now the Onion Routing is ready. The project comes with a simple DemoClient which sends a message to port 1010. Launching the program on port 1010, the program does not work as a node but as a simple demo server, which takes in a string message and returns a little response with the original message and some extra text. To run the demo, run these commands:

For the server (run this first):
> java -jar target/onion-1.0.jar 1010

Then:
> java -cp target/onion-1.0.jar no.ntnu.onion.DemoClient

You can now send messages that are "onion routed"!

_Final note: You may also run multiple instances of the DemoClient, and everything should still work!_

## Testing

To run the tests, enter the following terminal command when located within the project:

> mvn test

## References
- [Onion Routing](https://en.wikipedia.org/wiki/Onion_routing)
- [Diffie-Hellman Key Exchange](https://en.wikipedia.org/wiki/Onion_routing)
- [Computerphile on Onion Rouing](https://www.youtube.com/watch?v=QRYzre4bf7I&ab_channel=Computerphile)
