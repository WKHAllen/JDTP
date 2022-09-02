# Data Transfer Protocol for Java

Modern networking interfaces for Java.

## Data Transfer Protocol

The Data Transfer Protocol (DTP) is a larger project to make ergonomic network programming available in any language.
See the full project [here](https://wkhallen.com/dtp/).

## Creating a server

A server can be built using the `Server` implementation:

```java
import jdtp.Server;

// Create a server that receives strings and returns the length of each string
class MyServer extends Server {
    @Override
    protected void receive(long clientID, Object data) {
        // Send back the length of the string
        String message = (String) data;
        send(clientID, message.length());
    }

    @Override
    protected void connect(long clientID) {
        System.out.printf("Client with ID %d connected\n", clientID);
    }

    @Override
    protected void disconnect(long clientID) {
        System.out.printf("Client with ID %d disconnected\n", clientID);
    }
}

class Main {
    public static void main(String[] args) {
        // Start the server
        MyServer server = new MyServer();
        server.start("127.0.0.1", 29275);
    }
}
```

## Creating a client

A client can be built using the `Client` implementation:

```java
import jdtp.Client;

// Create a client that sends a message to the server and receives the length of the message
class MyClient extends Client {
    private final String message;

    MyClient(String message) {
        this.message = message;
    }

    @Override
    protected void receive(Object data) {
        // Validate the response
        int intData = (int) data;
        System.out.printf("Received response from server: %d\n", intData);
        assert intData == message.length();
    }

    @Override
    protected void disconnected() {
        System.err.println("Unexpectedly disconnected from server");
    }
}

class Main {
    public static void main(String[] args) {
        // Connect to the server
        String message = "Hello, server!";
        MyClient client = new MyClient(message);
        client.connect("127.0.0.1", 29275);

        // Send a message to the server
        client.send(message);
    }
}
```

## Security

Information security comes included. Every message sent over a network interface is encrypted with AES-256. Key
exchanges are performed using a 4096-bit RSA key-pair.
