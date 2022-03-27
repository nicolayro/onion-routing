
# Anonyon - An Onion Routing Library

## Intro

This project will is an assignment for IDATT2104 - Nettverksprogrammering
The assignment is to implement **onion routing** either as software or as a library. 
We are free to use any programming language, but C++ or Rust implementations are more desired. Even though I would really like to learn more C++, given the complexity of the task I chose to use Java, as it is what I'm most comfortable with.

## Implemented functionality


## Plans

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
    - Still some work needed to make it a functional library for existing 

## External Libraries

This project has yet to make use of any external libraries

## Installation Guide

## Usage Guide

## Testing

## Documentation


## References
	- [Onion Routing](https://en.wikipedia.org/wiki/Onion_routing)
	- [Diffie-Hellman Key Exchange](https://en.wikipedia.org/wiki/Onion_routing)
	- [Computerphile on Onion Rouing](https://www.youtube.com/watch?v=QRYzre4bf7I&ab_channel=Computerphile)
