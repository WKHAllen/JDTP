package jdtp;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.HashMap;

public abstract class Server {
    private final boolean blocking;
    private final boolean eventBlocking;
    private boolean serving = false;
    private ServerSocket sock = null;
    private Thread serveThread = null;
    private HashMap<Long, Socket> clients = new HashMap<Long, Socket>();
    private long nextClientID = 0;

    public Server(boolean blocking_, boolean eventBlocking_) {
        blocking = blocking_;
        eventBlocking = eventBlocking_;
    }

    public Server() {
        this(false, false);
    }

    public void start(String host, int port) throws JDTPException, UnknownHostException, IOException {
        if (serving) {
            throw new JDTPException("server is already serving");
        }

        InetSocketAddress address = new InetSocketAddress(host, port);

        sock = new ServerSocket();
        sock.bind(address, Util.listenBacklog);

        serving = true;
        callServe();
    }

    public void start(String host) {}

    public void start(int port) {}

    public void start() {}

    public void stop() {}

    public void send() {}

    public void sendAll() {}

    public void removeClient(long clientID) {}

    public boolean isServing() {
        return serving;
    }

    public String getHost() throws JDTPException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        return sock.getInetAddress().getHostAddress();
    }

    public int getPort() throws JDTPException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        return sock.getLocalPort();
    }

    public String getClientHost(long clientID) throws JDTPException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        Socket client = clients.get(clientID);

        if (client != null) {
            return client.getInetAddress().getHostAddress();
        } else {
            throw new JDTPException("client does not exist");
        }
    }

    public int getClientPort(long clientID) throws JDTPException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        Socket client = clients.get(clientID);

        if (client != null) {
            return client.getPort();
        } else {
            throw new JDTPException("client does not exist");
        }
    }

    private long newClientID() {
        return nextClientID++;
    }

    private void callServe() {}

    private void serve() {}

    private void callReceive(long clientID, byte[] data) {}

    private void callConnect(long clientID) {}

    private void callDisconnect(long clientID) {}

    protected abstract void receive(long clientID, byte[] data);

    protected abstract void connect(long clientID);

    protected abstract void disconnect(long clientID);
}
