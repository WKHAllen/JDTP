package jdtp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * A socket client.
 */
public abstract class Client {
    /**
     * Whether the client will block while connected to a server.
     */
    private final boolean blocking;

    /**
     * Whether the client will block while calling event methods.
     */
    private final boolean eventBlocking;

    /**
     * Whether the client is currently connected to a server.
     */
    private boolean connected = false;

    /**
     * The client socket.
     */
    private SocketChannel sock = null;

    /**
     * The thread from which the client will handle data received from the server.
     */
    private Thread handleThread = null;

    /**
     * Instantiate a socket client.
     *
     * @param blocking_      Whether the client should block while connected to a server.
     * @param eventBlocking_ Whether the client should block while calling event methods.
     */
    public Client(boolean blocking_, boolean eventBlocking_) {
        blocking = blocking_;
        eventBlocking = eventBlocking_;
    }

    /**
     * Instantiate a socket client which doesn't block while connected to a server or calling event methods.
     */
    public Client() {
        this(false, false);
    }

    /**
     * Connect to a server.
     *
     * @param host The server host.
     * @param port The server port.
     * @throws JDTPException If the client is already connected to a server.
     * @throws IOException   If an error occurs while connecting to the server.
     */
    public void connect(String host, int port) throws JDTPException, IOException {
        if (connected) {
            throw new JDTPException("client is already connected to a server");
        }

        InetSocketAddress address = new InetSocketAddress(host, port);

        sock = SocketChannel.open();
        sock.connect(address);

        connected = true;
        callHandle();
    }

    /**
     * Connect to a server, using the default port.
     *
     * @param host The server host.
     * @throws JDTPException If the client is already connected to a server.
     * @throws IOException   If an error occurs while connecting to the server.
     */
    public void connect(String host) throws JDTPException, IOException {
        connect(host, Util.defaultPort);
    }

    /**
     * Connect to a server, using the default host.
     *
     * @param port The server port.
     * @throws JDTPException If the client is already connected to a server.
     * @throws IOException   If an error occurs while connecting to the server.
     */
    public void connect(int port) throws JDTPException, IOException {
        connect(Util.defaultHost(), port);
    }

    /**
     * Connect to a server, using the default host and port.
     *
     * @throws JDTPException If the client is already connected to a server.
     * @throws IOException   If an error occurs while connecting to the server.
     */
    public void connect() throws JDTPException, IOException {
        connect(Util.defaultHost(), Util.defaultPort);
    }

    /**
     * Disconnect from the server.
     *
     * @throws JDTPException        If the client is not connected to a server.
     * @throws IOException          If an error occurs while disconnecting from the server.
     * @throws InterruptedException If an error occurs while waiting for the handle thread to join.
     */
    public void disconnect() throws JDTPException, IOException, InterruptedException {
        if (!connected) {
            throw new JDTPException("client is not connected to a server");
        }

        connected = false;

        sock.close();

        if (handleThread != null && handleThread != Thread.currentThread()) {
            handleThread.join();
        }
    }

    /**
     * Send data to the server.
     *
     * @param data The data to send.
     * @throws JDTPException If the client is not connected to a server.
     * @throws IOException   If an error occurs while sending the data.
     */
    public void send(byte[] data) throws JDTPException, IOException {
        if (!connected) {
            throw new JDTPException("client is not connected to a server");
        }

        byte[] encodedData = Util.encodeMessage(data);
        ByteBuffer encodedDataBuffer = ByteBuffer.wrap(encodedData);
        sock.write(encodedDataBuffer);
    }

    /**
     * Check if the client is connected to a server.
     *
     * @return Whether the client is connected to a server.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Get the host address of the client.
     *
     * @return The host address of the client.
     * @throws JDTPException If the client is not connected to a server.
     * @throws IOException   If an error occurs while checking the host address of the client.
     */
    public String getHost() throws JDTPException, IOException {
        if (!connected) {
            throw new JDTPException("client is not connected to a server");
        }

        InetSocketAddress address = (InetSocketAddress) sock.getLocalAddress();
        return address.getAddress().getHostAddress();
    }

    /**
     * Get the port of the client.
     *
     * @return The port of the client.
     * @throws JDTPException If the client is not connected to a server.
     * @throws IOException   If an error occurs while checking the port of the client.
     */
    public int getPort() throws JDTPException, IOException {
        if (!connected) {
            throw new JDTPException("client is not connected to a server");
        }

        InetSocketAddress address = (InetSocketAddress) sock.getLocalAddress();
        return address.getPort();
    }

    /**
     * Get the host address of the server.
     *
     * @return The host address of the server.
     * @throws JDTPException If the client is not connected to a server.
     * @throws IOException   If an error occurs while checking the host address of the server.
     */
    public String getServerHost() throws JDTPException, IOException {
        if (!connected) {
            throw new JDTPException("client is not connected to a server");
        }

        InetSocketAddress address = (InetSocketAddress) sock.getRemoteAddress();
        return address.getAddress().getHostAddress();
    }

    /**
     * Get the port of the server.
     *
     * @return The port of the server.
     * @throws JDTPException If the client is not connected to a server.
     * @throws IOException   If an error occurs while checking the port of the server.
     */
    public int getServerPort() throws JDTPException, IOException {
        if (!connected) {
            throw new JDTPException("client is not connected to a server");
        }

        InetSocketAddress address = (InetSocketAddress) sock.getRemoteAddress();
        return address.getPort();
    }

    /**
     * Call the handle method.
     *
     * @throws IOException If an error occurs while handling data received from the server.
     */
    private void callHandle() throws IOException {
        if (blocking) {
            handle();
        } else {
            handleThread = new Thread(() -> {
                try {
                    handle();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            handleThread.start();
        }
    }

    /**
     * Handle data received from the server.
     *
     * @throws IOException If an error occurs while handling data received from the server.
     */
    private void handle() throws IOException {
        ByteBuffer sizeBuffer = ByteBuffer.allocate(Util.lenSize);

        while (connected) {
            sizeBuffer.clear();

            int bytesReceived = sock.read(sizeBuffer);

            if (bytesReceived != Util.lenSize) {
                break;
            }

            long messageSize = Util.decodeMessageSize(sizeBuffer.array());
            ByteBuffer messageBuffer = ByteBuffer.allocate((int) messageSize);

            bytesReceived = sock.read(messageBuffer);

            if (bytesReceived != messageSize) {
                break;
            }

            callReceive(messageBuffer.array());
        }

        if (connected) {
            connected = false;
            sock.close();

            callDisconnected();
        }
    }

    /**
     * Call the receive event method.
     *
     * @param data The data received from the server.
     */
    private void callReceive(byte[] data) {
        if (eventBlocking) {
            receive(data);
        } else {
            new Thread(() -> receive(data)).start();
        }
    }

    /**
     * Call the disconnected event method.
     */
    private void callDisconnected() {
        if (eventBlocking) {
            disconnected();
        } else {
            new Thread(() -> disconnected()).start();
        }
    }

    /**
     * An event method, called when data is received from the server.
     *
     * @param data The data received from the server.
     */
    protected abstract void receive(byte[] data);

    /**
     * An event method, called when the server has disconnected the client.
     */
    protected abstract void disconnected();
}
