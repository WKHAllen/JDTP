package jdtp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A socket server.
 */
public abstract class Server {
    /**
     * Whether the server is currently serving.
     */
    private boolean serving = false;

    /**
     * The server socket selector.
     */
    private Selector selector = null;

    /**
     * The server socket.
     */
    private ServerSocketChannel sock = null;

    /**
     * The thread from which the server will serve clients.
     */
    private Thread serveThread = null;

    /**
     * A collection of the client sockets.
     */
    private final HashMap<Long, SocketChannel> clients = new HashMap<>();

    /**
     * The next available client ID.
     */
    private long nextClientID = 0;

    /**
     * Instantiate a socket server.
     */
    public Server() {
    }

    /**
     * Start the socket server.
     *
     * @param host The address to host the server on.
     * @param port The port to host the server on.
     * @throws JDTPException If the server is already serving.
     * @throws IOException   If an error occurs while starting the server.
     */
    public void start(String host, int port) throws JDTPException, IOException {
        if (serving) {
            throw new JDTPException("server is already serving");
        }

        InetSocketAddress address = new InetSocketAddress(host, port);

        selector = Selector.open();

        sock = ServerSocketChannel.open();
        sock.bind(address, Util.listenBacklog);
        sock.configureBlocking(false);
        sock.register(selector, SelectionKey.OP_ACCEPT);

        serving = true;
        callServe();
    }

    /**
     * Start the socket server, using the default port.
     *
     * @param host The address to host the server on.
     * @throws JDTPException If the server is already serving.
     * @throws IOException   If an error occurs while starting the server.
     */
    public void start(String host) throws JDTPException, IOException {
        start(host, Util.defaultPort);
    }

    /**
     * Start the socket server, using the default host.
     *
     * @param port The port to host the server on.
     * @throws JDTPException If the server is already serving.
     * @throws IOException   If an error occurs while starting the server.
     */
    public void start(int port) throws JDTPException, IOException {
        start(Util.defaultHost(), port);
    }

    /**
     * Start the socket server, using the default host and port.
     *
     * @throws JDTPException If the server is already serving.
     * @throws IOException   If an error occurs while starting the server.
     */
    public void start() throws JDTPException, IOException {
        start(Util.defaultHost(), Util.defaultPort);
    }

    /**
     * Stop the server.
     *
     * @throws JDTPException        If the server is not serving.
     * @throws IOException          If an error occurs while stopping the server.
     * @throws InterruptedException If an error occurs while waiting for the serve thread to join.
     */
    public void stop() throws JDTPException, IOException, InterruptedException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        serving = false;

        for (Map.Entry<Long, SocketChannel> client : clients.entrySet()) {
            client.getValue().close();
            clients.remove(client.getKey());
        }

        sock.close();
        selector.close();

        if (serveThread != null && serveThread != Thread.currentThread()) {
            serveThread.join();
        }

    }

    /**
     * Send data to a client.
     *
     * @param clientID The ID of the client to send the data to.
     * @param data     The data to send.
     * @throws JDTPException If the server is not serving, or if the specified client does not exist.
     * @throws IOException   If an error occurs while sending the data.
     */
    public void send(long clientID, Object data) throws JDTPException, IOException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        SocketChannel clientSock = clients.get(clientID);

        if (clientSock != null) {
            byte[] serializedData = Util.serialize(data);
            byte[] encodedData = Util.encodeMessage(serializedData);
            ByteBuffer encodedDataBuffer = ByteBuffer.wrap(encodedData);
            clientSock.write(encodedDataBuffer);
        } else {
            throw new JDTPException("client does not exist");
        }
    }

    /**
     * Send data to all clients.
     *
     * @param data The data to send.
     * @throws JDTPException If the server is not serving.
     * @throws IOException   If an error occurs while sending the data.
     */
    public void sendAll(Object data) throws JDTPException, IOException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        byte[] serializedData = Util.serialize(data);
        byte[] encodedData = Util.encodeMessage(serializedData);

        for (Map.Entry<Long, SocketChannel> client : clients.entrySet()) {
            SocketChannel clientSock = client.getValue();
            ByteBuffer encodedDataBuffer = ByteBuffer.wrap(encodedData);
            clientSock.write(encodedDataBuffer);
        }
    }

    /**
     * Disconnect a client from the server.
     *
     * @param clientID The ID of the client to disconnect.
     * @throws JDTPException If the server is not serving, or if the specified client does not exist.
     * @throws IOException   If an error occurs while disconnecting the client.
     */
    public void removeClient(long clientID) throws JDTPException, IOException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        SocketChannel client = clients.get(clientID);

        if (client != null) {
            client.close();
            clients.remove(clientID);
        } else {
            throw new JDTPException("client does not exist");
        }
    }

    /**
     * Check if the server is serving.
     *
     * @return Whether the server is serving.
     */
    public boolean isServing() {
        return serving;
    }

    /**
     * Get the host address of the server.
     *
     * @return The host address of the server.
     * @throws JDTPException If the server is not serving.
     * @throws IOException   If an error occurs while checking the host address of the server.
     */
    public String getHost() throws JDTPException, IOException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        InetSocketAddress address = (InetSocketAddress) sock.getLocalAddress();
        return address.getAddress().getHostAddress();
    }

    /**
     * Get the port of the server.
     *
     * @return The port of the server.
     * @throws JDTPException If the server is not serving.
     * @throws IOException   If an error occurs while checking the port of the server.
     */
    public int getPort() throws JDTPException, IOException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        InetSocketAddress address = (InetSocketAddress) sock.getLocalAddress();
        return address.getPort();
    }

    /**
     * Get the host address of a client.
     *
     * @param clientID The ID of the client.
     * @return The host address of the client.
     * @throws JDTPException If the server is not serving, or if the specified client does not exist.
     * @throws IOException   If an error occurs while checking the host address of the client.
     */
    public String getClientHost(long clientID) throws JDTPException, IOException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        SocketChannel client = clients.get(clientID);

        if (client != null) {
            InetSocketAddress address = (InetSocketAddress) client.getRemoteAddress();
            return address.getAddress().getHostAddress();
        } else {
            throw new JDTPException("client does not exist");
        }
    }

    /**
     * Get the port of a client.
     *
     * @param clientID The ID of the client.
     * @return The port of the client.
     * @throws JDTPException If the server is not serving, or if the specified client does not exist.
     * @throws IOException   If an error occurs while checking the port of the client.
     */
    public int getClientPort(long clientID) throws JDTPException, IOException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        SocketChannel client = clients.get(clientID);

        if (client != null) {
            InetSocketAddress address = (InetSocketAddress) client.getRemoteAddress();
            return address.getPort();
        } else {
            throw new JDTPException("client does not exist");
        }
    }

    /**
     * Get the next available client ID.
     *
     * @return The next available client ID.
     */
    private long newClientID() {
        return nextClientID++;
    }

    /**
     * Call the serve method.
     *
     * @throws IOException If an error occurs while serving.
     */
    private void callServe() throws IOException {
        serveThread = new Thread(() -> {
            try {
                serve();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        serveThread.start();
    }

    /**
     * Serve clients.
     *
     * @throws IOException If an error occurs while serving.
     */
    private void serve() throws IOException {
        ByteBuffer sizeBuffer = ByteBuffer.allocate(Util.lenSize);

        while (serving) {
            selector.select();
            Set<SelectionKey> selectedKeys;

            try {
                selectedKeys = selector.selectedKeys();
            } catch (ClosedSelectorException e) {
                return;
            }

            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();

                if (key.isAcceptable()) {
                    SocketChannel client = sock.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);

                    long clientID = newClientID();
                    clients.put(clientID, client);

                    callConnect(clientID);
                } else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    long clientID = clients
                            .entrySet()
                            .stream()
                            .filter(entry -> Objects.equals(entry.getValue(), client))
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList())
                            .get(0);
                    sizeBuffer.clear();

                    int bytesReceived = -1;

                    try {
                        bytesReceived = client.read(sizeBuffer);
                    } catch (IOException e) {
                        if (clients.containsKey(clientID)) {
                            client.close();
                            clients.remove(clientID);

                            callDisconnect(clientID);
                            continue;
                        }
                    }

                    if (bytesReceived != Util.lenSize) {
                        if (clients.containsKey(clientID)) {
                            client.close();
                            clients.remove(clientID);

                            callDisconnect(clientID);
                            continue;
                        }
                    }

                    long messageSize = Util.decodeMessageSize(sizeBuffer.array());
                    ByteBuffer messageBuffer = ByteBuffer.allocate((int) messageSize);

                    try {
                        bytesReceived = client.read(messageBuffer);
                    } catch (IOException e) {
                        if (clients.containsKey(clientID)) {
                            client.close();
                            clients.remove(clientID);

                            callDisconnect(clientID);
                            continue;
                        }
                    }

                    if (bytesReceived != messageSize) {
                        if (clients.containsKey(clientID)) {
                            client.close();
                            clients.remove(clientID);

                            callDisconnect(clientID);
                            continue;
                        }
                    }

                    callReceive(clientID, messageBuffer.array());
                }

                iter.remove();
            }
        }
    }

    /**
     * Call the receive event method.
     *
     * @param clientID The ID of the client who sent the data.
     * @param data     The data received from the client.
     */
    private void callReceive(long clientID, byte[] data) {
        final Object deserializedData;

        try {
            deserializedData = Util.deserialize(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        new Thread(() -> receive(clientID, deserializedData)).start();
    }

    /**
     * Call the connect event method.
     *
     * @param clientID The ID of the client who connected.
     */
    private void callConnect(long clientID) {
        new Thread(() -> connect(clientID)).start();
    }

    /**
     * Call the disconnect event method.
     *
     * @param clientID The ID of the client who disconnected.
     */
    private void callDisconnect(long clientID) {
        new Thread(() -> disconnect(clientID)).start();
    }

    /**
     * An event method, called when data is received from a client.
     *
     * @param clientID The ID of the client who sent the data.
     * @param data     The data received from the client.
     */
    protected abstract void receive(long clientID, Object data);

    /**
     * An event method, called when a client connects.
     *
     * @param clientID The ID of the client who connected.
     */
    protected abstract void connect(long clientID);

    /**
     * An event method, called when a client disconnects.
     *
     * @param clientID The ID of the client who disconnected.
     */
    protected abstract void disconnect(long clientID);
}
