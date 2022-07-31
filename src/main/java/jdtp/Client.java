package jdtp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class Client {
    private final boolean blocking;
    private final boolean eventBlocking;
    private boolean connected = false;
    private SocketChannel sock = null;
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

        sock = SocketChannel.open();
        sock.connect(address);

        connected = true;
        callHandle();
    }

    public void connect(String host) throws JDTPException, IOException {
        connect(host, Util.defaultPort);
    }

    public void connect(int port) throws JDTPException, IOException {
        connect(Util.defaultHost(), port);
    }

    public void connect() throws JDTPException, IOException {
        connect(Util.defaultHost(), Util.defaultPort);
    }

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

    public void send(byte[] data) throws JDTPException, IOException {
        if (!connected) {
            throw new JDTPException("client is not connected to a server");
        }

        byte[] encodedData = Util.encodeMessage(data);
        ByteBuffer encodedDataBuffer = ByteBuffer.wrap(encodedData);
        sock.write(encodedDataBuffer);
    }

    public boolean isConnected() {
        return connected;
    }

    public String getHost() throws JDTPException, IOException {
        if (!connected) {
            throw new JDTPException("client is not connected to a server");
        }

        InetSocketAddress address = (InetSocketAddress) sock.getLocalAddress();
        return address.getAddress().getHostAddress();
    }

    public int getPort() throws JDTPException, IOException {
        if (!connected) {
            throw new JDTPException("client is not connected to a server");
        }

        InetSocketAddress address = (InetSocketAddress) sock.getLocalAddress();
        return address.getPort();
    }

    public String getServerHost() throws JDTPException, IOException {
        if (!connected) {
            throw new JDTPException("client is not connected to a server");
        }

        InetSocketAddress address = (InetSocketAddress) sock.getRemoteAddress();
        return address.getAddress().getHostAddress();
    }

    public int getServerPort() throws JDTPException, IOException {
        if (!connected) {
            throw new JDTPException("client is not connected to a server");
        }

        InetSocketAddress address = (InetSocketAddress) sock.getRemoteAddress();
        return address.getPort();
    }

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

    private void callReceive(byte[] data) {
        if (eventBlocking) {
            receive(data);
        } else {
            new Thread(() -> receive(data)).start();
        }
    }

    private void callDisconnected() {
        if (eventBlocking) {
            disconnected();
        } else {
            new Thread(() -> disconnected()).start();
        }
    }

    protected abstract void receive(byte[] data);

    protected abstract void disconnected();
}
