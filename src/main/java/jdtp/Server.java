package jdtp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Server {
    private final boolean blocking;
    private final boolean eventBlocking;
    private boolean serving = false;
    private Selector selector = null;
    private ServerSocketChannel sock = null;
    private Thread serveThread = null;
    private final HashMap<Long, SocketChannel> clients = new HashMap<Long, SocketChannel>();
    private long nextClientID = 0;

    public Server(boolean blocking_, boolean eventBlocking_) {
        blocking = blocking_;
        eventBlocking = eventBlocking_;
    }

    public Server() {
        this(false, false);
    }

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

    public void start(String host) throws JDTPException, IOException {
        start(host, Util.defaultPort);
    }

    public void start(int port) throws JDTPException, IOException {
        start(Util.defaultHost(), port);
    }

    public void start() throws JDTPException, IOException {
        start(Util.defaultHost(), Util.defaultPort);
    }

    public void stop() throws JDTPException, IOException, InterruptedException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        serving = false;

        for (Map.Entry<Long, SocketChannel> client : clients.entrySet()) {
            removeClient(client.getKey());
        }

        sock.close();

        if (serveThread != null && serveThread != Thread.currentThread()) {
            serveThread.join();
        }

        selector.close();
    }

    public void send(long clientID, byte[] data) throws JDTPException, IOException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        SocketChannel clientSock = clients.get(clientID);

        if (clientSock != null) {
            byte[] encodedData = Util.encodeMessage(data);
            ByteBuffer encodedDataBuffer = ByteBuffer.wrap(encodedData);
            clientSock.write(encodedDataBuffer);
        } else {
            throw new JDTPException("client does not exist");
        }
    }

    public void sendAll(byte[] data) throws JDTPException, IOException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        byte[] encodedData = Util.encodeMessage(data);

        for (Map.Entry<Long, SocketChannel> client : clients.entrySet()) {
            SocketChannel clientSock = client.getValue();
            ByteBuffer encodedDataBuffer = ByteBuffer.wrap(encodedData);
            clientSock.write(encodedDataBuffer);
        }
    }

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

    public boolean isServing() {
        return serving;
    }

    public String getHost() throws JDTPException, IOException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        InetSocketAddress address = (InetSocketAddress) sock.getLocalAddress();
        return address.getAddress().getHostAddress();
    }

    public int getPort() throws JDTPException, IOException {
        if (!serving) {
            throw new JDTPException("server is not serving");
        }

        InetSocketAddress address = (InetSocketAddress) sock.getLocalAddress();
        return address.getPort();
    }

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

    private long newClientID() {
        return nextClientID++;
    }

    private void callServe() throws IOException {
        if (blocking) {
            serve();
        } else {
            serveThread = new Thread(() -> {
                try {
                    serve();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            serveThread.start();
        }
    }

    private void serve() throws IOException {
        ByteBuffer sizeBuffer = ByteBuffer.allocate(Util.lenSize);

        while (serving) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
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

                    int bytesReceived = client.read(sizeBuffer);

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

                    bytesReceived = client.read(messageBuffer);

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

    private void callReceive(long clientID, byte[] data) {
        if (eventBlocking) {
            receive(clientID, data);
        } else {
            new Thread(() -> receive(clientID, data)).start();
        }
    }

    private void callConnect(long clientID) {
        if (eventBlocking) {
            connect(clientID);
        } else {
            new Thread(() -> connect(clientID)).start();
        }
    }

    private void callDisconnect(long clientID) {
        if (eventBlocking) {
            disconnect(clientID);
        } else {
            new Thread(() -> disconnect(clientID)).start();
        }
    }

    protected abstract void receive(long clientID, byte[] data);

    protected abstract void connect(long clientID);

    protected abstract void disconnect(long clientID);
}
