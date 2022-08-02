package jdtp;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

class JDTPTest {
    @Test
    void TestJDTP() throws JDTPException, IOException, InterruptedException {
        int waitTime = 100;

        // Generate large random messages
        Random random = new Random();
        long randomMessageToServer = random.nextLong();
        long randomMessageToClient = random.nextLong();
        System.out.printf("Large random messages: %d, %d\n", randomMessageToServer, randomMessageToClient);

        // Begin testing
        System.out.println("Running tests...");

        // Start server
        TestServer server = new TestServer();
        server.randomMessage = randomMessageToServer;
        server.start();

        Thread.sleep(waitTime);

        // Get server host and port
        String serverHost = server.getHost();
        int serverPort = server.getPort();
        System.out.printf("Server host: %s\n", serverHost);
        System.out.printf("Server port: %d\n", serverPort);

        // Test that the client does not exist
        try {
            server.removeClient(0);
            System.out.println("Did not throw on removal of unknown client");
            assert false;
        } catch (JDTPException e) {
            System.out.printf("Throws on removal of unknown client: '%s'\n", e.getMessage());
        }

        Thread.sleep(waitTime);

        // Start client
        TestClient client = new TestClient();
        client.randomMessage = randomMessageToClient;
        client.connect();

        Thread.sleep(waitTime);

        // Get client host and port
        String clientHost = client.getHost();
        int clientPort = client.getPort();
        System.out.printf("Client host: %s\n", clientHost);
        System.out.printf("Client port: %d\n", clientPort);

        // Check server and client host and port line up
        assert Objects.equals(server.getHost(), client.getServerHost());
        assert server.getPort() == client.getServerPort();
        assert Objects.equals(server.getClientHost(0), client.getHost());
        assert server.getClientPort(0) == client.getPort();
        System.out.println("Server and client host and port line up");

        Thread.sleep(waitTime);

        // Client send
        String clientMessage = "Hello, server";
        client.send(clientMessage);

        Thread.sleep(waitTime);

        // Server send
        String serverMessage = "Hello, client #0";
        server.send(0, serverMessage);

        Thread.sleep(waitTime);

        server.receivingRandomMessage = true;
        client.receivingRandomMessage = true;

        Thread.sleep(waitTime);

        // Client send large message
        client.send(randomMessageToServer);

        Thread.sleep(waitTime);

        // Server send large message
        server.sendAll(randomMessageToClient);

        Thread.sleep(waitTime);

        server.receivingRandomMessage = false;
        client.receivingRandomMessage = false;

        Thread.sleep(waitTime);

        // Client disconnect
        client.disconnect();

        Thread.sleep(waitTime);

        // Server stop
        server.stop();

        // Done
        System.out.println("Successfully passed all tests");
    }
}
