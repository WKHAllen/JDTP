package jdtp;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

class JDTPTest {
    private final int waitTime = 100;
    private final Random random = new Random();

    @Test
    void TestUtil() throws IOException, ClassNotFoundException {
        boolean randomBool = random.nextBoolean();
        int randomInt = random.nextInt();
        long randomLong = random.nextLong();
        float randomFloat = random.nextFloat();
        double randomDouble = random.nextDouble();
        byte[] randomBytes = {0, 0, 0, 0, 0, 0, 0, 0};
        random.nextBytes(randomBytes);
        String randomString = randomBytes.toString();

        assert (boolean) Util.deserialize(Util.serialize(randomBool)) == randomBool;
        assert (int) Util.deserialize(Util.serialize(randomInt)) == randomInt;
        assert (long) Util.deserialize(Util.serialize(randomLong)) == randomLong;
        assert (float) Util.deserialize(Util.serialize(randomFloat)) == randomFloat;
        assert (double) Util.deserialize(Util.serialize(randomDouble)) == randomDouble;
        assert Arrays.equals((byte[]) Util.deserialize(Util.serialize(randomBytes)), randomBytes);
        assert ((String) Util.deserialize(Util.serialize(randomString))).equals(randomString);

        assert Arrays.equals(Util.encodeMessageSize(0), new byte[]{(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0});
        assert Arrays.equals(Util.encodeMessageSize(1), new byte[]{(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x1});
        assert Arrays.equals(Util.encodeMessageSize(255), new byte[]{(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0xff});
        assert Arrays.equals(Util.encodeMessageSize(256), new byte[]{(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x1, (byte) 0x0});
        assert Arrays.equals(Util.encodeMessageSize(257), new byte[]{(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x1, (byte) 0x1});
        assert Arrays.equals(Util.encodeMessageSize(4311810305L), new byte[]{(byte) 0x1, (byte) 0x1, (byte) 0x1, (byte) 0x1, (byte) 0x1});
        assert Arrays.equals(Util.encodeMessageSize(4328719365L), new byte[]{(byte) 0x1, (byte) 0x2, (byte) 0x3, (byte) 0x4, (byte) 0x5});
        assert Arrays.equals(Util.encodeMessageSize(47362409218L), new byte[]{(byte) 0xb, (byte) 0x7, (byte) 0x5, (byte) 0x3, (byte) 0x2});
        assert Arrays.equals(Util.encodeMessageSize(1099511627775L), new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff});

        assert Util.decodeMessageSize(new byte[]{(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0}) == 0;
        assert Util.decodeMessageSize(new byte[]{(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x1}) == 1;
        assert Util.decodeMessageSize(new byte[]{(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0xff}) == 255;
        assert Util.decodeMessageSize(new byte[]{(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x1, (byte) 0x0}) == 256;
        assert Util.decodeMessageSize(new byte[]{(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x1, (byte) 0x1}) == 257;
        assert Util.decodeMessageSize(new byte[]{(byte) 0x1, (byte) 0x1, (byte) 0x1, (byte) 0x1, (byte) 0x1}) == 4311810305L;
        assert Util.decodeMessageSize(new byte[]{(byte) 0x1, (byte) 0x2, (byte) 0x3, (byte) 0x4, (byte) 0x5}) == 4328719365L;
        assert Util.decodeMessageSize(new byte[]{(byte) 0xb, (byte) 0x7, (byte) 0x5, (byte) 0x3, (byte) 0x2}) == 47362409218L;
        assert Util.decodeMessageSize(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}) == 1099511627775L;
    }

    @Test
    void TestCrypto() {

    }

    @Test
    void TestServerServe() throws JDTPException, IOException, InterruptedException {
        // Create server
        TestServer s = new TestServer(0, 0, 0);
        assert !s.isServing();

        // Start server
        s.start();
        assert s.isServing();
        Thread.sleep(waitTime);

        // Check server address info
        String serverHost = s.getHost();
        int serverPort = s.getPort();
        System.out.printf("Server address: %s:%d\n", serverHost, serverPort);

        // Stop server
        s.stop();
        assert !s.isServing();
        Thread.sleep(waitTime);

        // Check event counts
        assert s.getReceiveCount() == 0;
        assert s.getConnectCount() == 0;
        assert s.getDisconnectCount() == 0;
        assert s.eventsDone();
    }

    @Test
    void TestAddresses() throws JDTPException, IOException, InterruptedException {
        // Create server
        TestServer s = new TestServer(0, 1, 1);
        assert !s.isServing();
        s.start();
        assert s.isServing();
        String serverHost = s.getHost();
        int serverPort = s.getPort();
        System.out.printf("Server address: %s:%d\n", serverHost, serverPort);
        Thread.sleep(waitTime);

        // Create client
        TestClient c = new TestClient(0, 0);
        assert !c.isConnected();
        c.connect(serverHost, serverPort);
        assert c.isConnected();
        Thread.sleep(waitTime);

        // Check addresses match
        assert s.getHost().equals(c.getServerHost());
        assert s.getPort() == c.getServerPort();
        assert c.getHost().equals(s.getClientHost(0));
        assert c.getPort() == s.getClientPort(0);

        // Disconnect client
        c.disconnect();
        assert !c.isConnected();
        Thread.sleep(waitTime);

        // Stop server
        s.stop();
        assert !s.isServing();
        Thread.sleep(waitTime);

        // Check event counts
        assert s.getReceiveCount() == 0;
        assert s.getConnectCount() == 0;
        assert s.getDisconnectCount() == 0;
        assert s.eventsDone();
        assert Arrays.equals(s.getReceived(), new Object[]{});
        assert Arrays.equals(s.getReceivedClientIDs(), new long[]{});
        assert Arrays.equals(s.getConnectClientIDs(), new long[]{0});
        assert Arrays.equals(s.getDisconnectClientIDs(), new long[]{0});
        assert c.getReceiveCount() == 0;
        assert c.getDisconnectedCount() == 0;
        assert c.eventsDone();
        assert Arrays.equals(c.getReceived(), new Object[]{});
    }

    @Test
    void TestSendReceive() throws JDTPException, IOException, InterruptedException {
        // Create server
        TestServer s = new TestServer(1, 1, 1);
        s.start();
        String serverHost = s.getHost();
        int serverPort = s.getPort();
        System.out.printf("Server address: %s:%d\n", serverHost, serverPort);
        Thread.sleep(waitTime);

        // Create client
        TestClient c = new TestClient(1, 0);
        c.connect(serverHost, serverPort);
        Thread.sleep(waitTime);

        // Send messages
        String serverMessage = "Hello, server!";
        String clientMessage = "Hello, client #0!";
        c.send(serverMessage);
        s.send(0, clientMessage);
        Thread.sleep(waitTime);

        // Disconnect client
        c.disconnect();
        Thread.sleep(waitTime);

        // Stop server
        s.stop();
        Thread.sleep(waitTime);

        // Check event counts
        assert s.getReceiveCount() == 0;
        assert s.getConnectCount() == 0;
        assert s.getDisconnectCount() == 0;
        assert s.eventsDone();
        assert Arrays.equals(s.getReceived(), new Object[]{serverMessage});
        assert Arrays.equals(s.getReceivedClientIDs(), new long[]{0});
        assert Arrays.equals(s.getConnectClientIDs(), new long[]{0});
        assert Arrays.equals(s.getDisconnectClientIDs(), new long[]{0});
        assert c.getReceiveCount() == 0;
        assert c.getDisconnectedCount() == 0;
        assert c.eventsDone();
        assert Arrays.equals(c.getReceived(), new Object[]{clientMessage});
    }

    @Test
    void TestSendDifferentTypes() throws JDTPException, IOException, InterruptedException {
        // Messages
        Object[] serverMessages = new Object[]{false, 11, "Hello from client #0!", 1.61803398, null};
        Object[] clientMessages = new Object[]{"Hello from the server!", 2.718, 4294967296L, true};

        // Create server
        TestServer s = new TestServer(serverMessages.length, 1, 1);
        s.start();
        String serverHost = s.getHost();
        int serverPort = s.getPort();
        System.out.printf("Server address: %s:%d\n", serverHost, serverPort);
        Thread.sleep(waitTime);

        // Create client
        TestClient c = new TestClient(clientMessages.length, 0);
        c.connect(serverHost, serverPort);
        Thread.sleep(waitTime);

        // Send messages
        for (Object serverMessage : serverMessages) {
            c.send(serverMessage);
        }
        for (Object clientMessage : clientMessages) {
            s.send(0, clientMessage);
        }
        Thread.sleep(waitTime);

        // Disconnect client
        c.disconnect();
        Thread.sleep(waitTime);

        // Stop server
        s.stop();
        Thread.sleep(waitTime);

        // Check event counts
        assert s.getReceiveCount() == 0;
        assert s.getConnectCount() == 0;
        assert s.getDisconnectCount() == 0;
        assert s.eventsDone();
        assert Arrays.equals(s.getReceived(), serverMessages);
        assert Arrays.equals(s.getConnectClientIDs(), new long[]{0});
        assert Arrays.equals(s.getDisconnectClientIDs(), new long[]{0});
        assert c.getReceiveCount() == 0;
        assert c.getDisconnectedCount() == 0;
        assert c.eventsDone();
        assert Arrays.equals(c.getReceived(), clientMessages);
    }

    @Test
    void TestSendLargeMessages() throws JDTPException, IOException, InterruptedException {
        // Create server
        TestServer s = new TestServer(1, 1, 1);
        s.start();
        String serverHost = s.getHost();
        int serverPort = s.getPort();
        System.out.printf("Server address: %s:%d\n", serverHost, serverPort);
        Thread.sleep(waitTime);

        // Create client
        TestClient c = new TestClient(1, 0);
        c.connect(serverHost, serverPort);
        Thread.sleep(waitTime);

        // Send messages
        byte[] largeServerMessage = new byte[random.nextInt(32768, 65536)];
        random.nextBytes(largeServerMessage);
        byte[] largeClientMessage = new byte[random.nextInt(16384, 32768)];
        random.nextBytes(largeClientMessage);
        c.send(largeServerMessage);
        s.sendAll(largeClientMessage);
        Thread.sleep(waitTime);

        // Disconnect client
        c.disconnect();
        Thread.sleep(waitTime);

        // Stop server
        s.stop();
        Thread.sleep(waitTime);

        // Check event counts
        assert s.getReceiveCount() == 0;
        assert s.getConnectCount() == 0;
        assert s.getDisconnectCount() == 0;
        assert s.eventsDone();
        assert Arrays.equals((byte[]) s.getReceived()[0], largeServerMessage);
        assert Arrays.equals(s.getReceivedClientIDs(), new long[]{0});
        assert Arrays.equals(s.getConnectClientIDs(), new long[]{0});
        assert Arrays.equals(s.getDisconnectClientIDs(), new long[]{0});
        assert c.getReceiveCount() == 0;
        assert c.getDisconnectedCount() == 0;
        assert c.eventsDone();
        assert Arrays.equals((byte[]) c.getReceived()[0], largeClientMessage);
    }

    @Test
    void TestSendingNumerousMessages() throws JDTPException, IOException, InterruptedException {
        // Messages
        int[] serverMessages = new int[random.nextInt(64, 128)];
        int[] clientMessages = new int[random.nextInt(128, 256)];
        for (int i = 0; i < serverMessages.length; i++) {
            serverMessages[i] = random.nextInt();
        }
        for (int i = 0; i < clientMessages.length; i++) {
            clientMessages[i] = random.nextInt();
        }

        // Create server
        TestServer s = new TestServer(serverMessages.length, 1, 1);
        s.start();
        String serverHost = s.getHost();
        int serverPort = s.getPort();
        System.out.printf("Server address: %s:%d\n", serverHost, serverPort);
        Thread.sleep(waitTime);

        // Create client
        TestClient c = new TestClient(clientMessages.length, 0);
        c.connect(serverHost, serverPort);
        Thread.sleep(waitTime);

        // Send messages
        for (int serverMessage : serverMessages) {
            c.send(serverMessage);
        }
        for (int clientMessage : clientMessages) {
            s.sendAll(clientMessage);
        }
        Thread.sleep(waitTime);

        // Disconnect client
        c.disconnect();
        Thread.sleep(waitTime);

        // Stop server
        s.stop();
        Thread.sleep(waitTime);

        // Check event counts
        assert s.getReceiveCount() == 0;
        assert s.getConnectCount() == 0;
        assert s.getDisconnectCount() == 0;
        assert s.eventsDone();
        assert Arrays.equals(Arrays.stream(Arrays.asList(s.getReceived()).toArray(new Integer[0])).mapToInt(x -> x).toArray(), serverMessages);
        assert Arrays.equals(s.getConnectClientIDs(), new long[]{0});
        assert Arrays.equals(s.getDisconnectClientIDs(), new long[]{0});
        assert c.getReceiveCount() == 0;
        assert c.getDisconnectedCount() == 0;
        assert c.eventsDone();
        assert Arrays.equals(Arrays.stream(Arrays.asList(c.getReceived()).toArray(new Integer[0])).mapToInt(x -> x).toArray(), clientMessages);
    }

    @Test
    void TestMultipleClients() throws JDTPException, IOException, InterruptedException {
        // Messages
        String messageFromClient1 = "Hello from client #1!";
        String messageFromClient2 = "Goodbye from client #2!";
        String messageFromServer = "Hello from the server :)";

        // Create server
        TestServer s = new TestServer(2, 2, 2);
        s.replyWithStringLength = true;
        s.start();
        String serverHost = s.getHost();
        int serverPort = s.getPort();
        System.out.printf("Server address: %s:%d\n", serverHost, serverPort);
        Thread.sleep(waitTime);

        // Create client 1
        TestClient c1 = new TestClient(2, 0);
        c1.connect(serverHost, serverPort);
        Thread.sleep(waitTime);

        // Check client 1 address info
        assert c1.getHost().equals(s.getClientHost(0));
        assert c1.getPort() == s.getClientPort(0);
        assert c1.getServerHost().equals(s.getHost());
        assert c1.getServerPort() == s.getPort();

        // Create client 2
        TestClient c2 = new TestClient(2, 0);
        c2.connect(serverHost, serverPort);
        Thread.sleep(waitTime);

        // Check client 2 address info
        assert c2.getHost().equals(s.getClientHost(1));
        assert c2.getPort() == s.getClientPort(1);
        assert c2.getServerHost().equals(s.getHost());
        assert c2.getServerPort() == s.getPort();

        // Send message from client 1
        c1.send(messageFromClient1);
        Thread.sleep(waitTime);

        // Send message from client 2
        c2.send(messageFromClient2);
        Thread.sleep(waitTime);

        // Send message to all clients
        s.sendAll(messageFromServer);
        Thread.sleep(waitTime);

        // Disconnect client 1
        c1.disconnect();
        Thread.sleep(waitTime);

        // Disconnect client 2
        c2.disconnect();
        Thread.sleep(waitTime);

        // Stop server
        s.stop();
        Thread.sleep(waitTime);

        // Check event counts
        assert s.getReceiveCount() == 0;
        assert s.getConnectCount() == 0;
        assert s.getDisconnectCount() == 0;
        assert s.eventsDone();
        assert Arrays.equals(s.getReceived(), new Object[]{messageFromClient1, messageFromClient2});
        assert Arrays.equals(s.getReceivedClientIDs(), new long[]{0, 1});
        assert Arrays.equals(s.getConnectClientIDs(), new long[]{0, 1});
        assert Arrays.equals(s.getDisconnectClientIDs(), new long[]{0, 1});
        assert c1.getReceiveCount() == 0;
        assert c1.getDisconnectedCount() == 0;
        assert c1.eventsDone();
        assert Arrays.equals(c1.getReceived(), new Object[]{messageFromClient1.length(), messageFromServer});
        assert c2.getReceiveCount() == 0;
        assert c2.getDisconnectedCount() == 0;
        assert c2.eventsDone();
        assert Arrays.equals(c2.getReceived(), new Object[]{messageFromClient2.length(), messageFromServer});
    }

    @Test
    void TestClientDisconnected() throws JDTPException, IOException, InterruptedException {
        // Create server
        TestServer s = new TestServer(0, 1, 0);
        assert !s.isServing();
        s.start();
        assert s.isServing();
        String serverHost = s.getHost();
        int serverPort = s.getPort();
        System.out.printf("Server address: %s:%d\n", serverHost, serverPort);
        Thread.sleep(waitTime);

        // Create client
        TestClient c = new TestClient(0, 1);
        assert !c.isConnected();
        c.connect(serverHost, serverPort);
        assert c.isConnected();
        Thread.sleep(waitTime);

        // Stop server
        assert s.isServing();
        assert c.isConnected();
        s.stop();
        assert !s.isServing();
        Thread.sleep(waitTime);
        assert !c.isConnected();

        // Check event counts
        assert s.getReceiveCount() == 0;
        assert s.getConnectCount() == 0;
        assert s.getDisconnectCount() == 0;
        assert s.eventsDone();
        assert Arrays.equals(s.getReceived(), new Object[]{});
        assert Arrays.equals(s.getReceivedClientIDs(), new long[]{});
        assert Arrays.equals(s.getConnectClientIDs(), new long[]{0});
        assert Arrays.equals(s.getDisconnectClientIDs(), new long[]{});
        assert c.getReceiveCount() == 0;
        assert c.getDisconnectedCount() == 0;
        assert c.eventsDone();
        assert Arrays.equals(c.getReceived(), new Object[]{});
    }

    @Test
    void TestRemoveClient() throws JDTPException, IOException, InterruptedException {
        // Create server
        TestServer s = new TestServer(0, 1, 0);
        assert !s.isServing();
        s.start();
        assert s.isServing();
        String serverHost = s.getHost();
        int serverPort = s.getPort();
        System.out.printf("Server address: %s:%d\n", serverHost, serverPort);
        Thread.sleep(waitTime);

        // Create client
        TestClient c = new TestClient(0, 1);
        assert !c.isConnected();
        c.connect(serverHost, serverPort);
        assert c.isConnected();
        Thread.sleep(waitTime);

        // Disconnect the client
        assert c.isConnected();
        s.removeClient(0);
        Thread.sleep(waitTime);
        assert !c.isConnected();

        // Stop server
        s.stop();
        assert !s.isServing();
        Thread.sleep(waitTime);

        // Check event counts
        assert s.getReceiveCount() == 0;
        assert s.getConnectCount() == 0;
        assert s.getDisconnectCount() == 0;
        assert s.eventsDone();
        assert Arrays.equals(s.getReceived(), new Object[]{});
        assert Arrays.equals(s.getReceivedClientIDs(), new long[]{});
        assert Arrays.equals(s.getConnectClientIDs(), new long[]{0});
        assert Arrays.equals(s.getDisconnectClientIDs(), new long[]{});
        assert c.getReceiveCount() == 0;
        assert c.getDisconnectedCount() == 0;
        assert c.eventsDone();
        assert Arrays.equals(c.getReceived(), new Object[]{});
    }

    @Test
    void TestServerClientAddressDefaults() throws JDTPException, IOException, InterruptedException {
        // Create server
        TestServer s1 = new TestServer(0, 1, 1);
        s1.start();
        String serverHost1 = s1.getHost();
        int serverPort1 = s1.getPort();
        System.out.printf("Server address: %s:%d\n", serverHost1, serverPort1);
        Thread.sleep(waitTime);

        // Create client
        TestClient c1 = new TestClient(0, 0);
        c1.connect();
        Thread.sleep(waitTime);

        // Disconnect client
        c1.disconnect();
        Thread.sleep(waitTime);

        // Stop server
        s1.stop();
        Thread.sleep(waitTime);

        // Create server with host
        TestServer s2 = new TestServer(0, 1, 1);
        s2.start("127.0.0.1");
        String serverHost2 = s2.getHost();
        int serverPort2 = s2.getPort();
        System.out.printf("Server address: %s:%d\n", serverHost2, serverPort2);
        Thread.sleep(waitTime);

        // Create client with host
        TestClient c2 = new TestClient(0, 0);
        c2.connect(serverHost2);
        Thread.sleep(waitTime);

        // Disconnect client
        c2.disconnect();
        Thread.sleep(waitTime);

        // Stop server
        s2.stop();
        Thread.sleep(waitTime);

        // Create server with port
        TestServer s3 = new TestServer(0, 1, 1);
        s3.start(35792);
        String serverHost3 = s3.getHost();
        int serverPort3 = s3.getPort();
        System.out.printf("Server address: %s:%d\n", serverHost3, serverPort3);
        Thread.sleep(waitTime);

        // Create client with port
        TestClient c3 = new TestClient(0, 0);
        c3.connect(serverPort3);
        Thread.sleep(waitTime);

        // Disconnect client
        c3.disconnect();
        Thread.sleep(waitTime);

        // Stop server
        s3.stop();
        Thread.sleep(waitTime);

        // Create server with host and port
        TestServer s4 = new TestServer(0, 1, 1);
        s4.start("127.0.0.1", 35792);
        String serverHost4 = s4.getHost();
        int serverPort4 = s4.getPort();
        System.out.printf("Server address: %s:%d\n", serverHost4, serverPort4);
        Thread.sleep(waitTime);

        // Create client
        TestClient c4 = new TestClient(0, 0);
        c4.connect(serverHost4, serverPort4);
        Thread.sleep(waitTime);

        // Disconnect client
        c4.disconnect();
        Thread.sleep(waitTime);

        // Stop server
        s4.stop();
        Thread.sleep(waitTime);
    }
}
