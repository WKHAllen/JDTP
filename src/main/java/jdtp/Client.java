package jdtp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class Client {
    private final boolean blocking;
    private final boolean eventBlocking;
    private boolean connected = false;
    private Socket sock = null;
    private Thread handleThread = null;

    public Client(boolean blocking_, boolean eventBlocking_) {
        blocking = blocking_;
        eventBlocking = eventBlocking_;
    }

    public Client() {
        this(false, false);
    }

    public void connect(String host, int port) throws JDTPException, IOException {
        if (connected) {
            throw new JDTPException("client is already connected to a server");
        }

        InetSocketAddress address = new InetSocketAddress(host, port);

        sock = new Socket();
        sock.connect(address);

        connected = true;
        callHandle();
    }

    public void connect(String host) {
        // TODO
    }

    public void connect(int port) {
        // TODO
    }

    public void connect() {
        // TODO
    }

    public void disconnect() {
        // TODO
    }

    public void send(byte[] data) {
        // TODO
    }

    public boolean isConnected() {
        return connected;
    }

    public String getHost() throws JDTPException {
        if (!connected) {
            throw new JDTPException("client is not connected to a server");
        }

        return sock.getLocalAddress().getHostAddress();
    }

    public int getPort() throws JDTPException {
        if (!connected) {
            throw new JDTPException("client is not connected to a server");
        }

        return sock.getLocalPort();
    }

    public String getServerHost() throws JDTPException {
        if (!connected) {
            throw new JDTPException("client is not connected to a server");
        }

        return sock.getInetAddress().getHostAddress();
    }

    public int getServerPort() throws JDTPException {
        if (!connected) {
            throw new JDTPException("client is not connected to a server");
        }

        return sock.getPort();
    }

    private void callHandle() {
        // TODO
    }

    private void handle() {
        // TODO
    }

    private void callReceive(byte[] data) {
        // TODO
    }

    private void callDisconnected() {
        // TODO
    }

    protected abstract void receive(byte[] data);

    protected abstract void disconnected();
}
