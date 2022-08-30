package jdtp;

import java.util.ArrayList;

class TestServer extends Server {
    private int receiveCount;
    private int connectCount;
    private int disconnectCount;
    private final ArrayList<Object> received = new ArrayList<>();
    private final ArrayList<Long> receivedClientIDs = new ArrayList<>();
    private final ArrayList<Long> connectClientIDs = new ArrayList<>();
    private final ArrayList<Long> disconnectClientIDs = new ArrayList<>();
    public boolean replyWithStringLength = false;

    TestServer(int receiveCount, int connectCount, int disconnectCount) {
        super();

        this.receiveCount = receiveCount;
        this.connectCount = connectCount;
        this.disconnectCount = disconnectCount;
    }

    public boolean eventsDone() {
        return receiveCount == 0 && connectCount == 0 && disconnectCount == 0;
    }

    public int getReceiveCount() {
        return receiveCount;
    }

    public int getConnectCount() {
        return connectCount;
    }

    public int getDisconnectCount() {
        return disconnectCount;
    }

    public Object[] getReceived() {
        return received.toArray();
    }

    public long[] getReceivedClientIDs() {
        return receivedClientIDs.stream().mapToLong(x -> x).toArray();
    }

    public long[] getConnectClientIDs() {
        return connectClientIDs.stream().mapToLong(x -> x).toArray();
    }

    public long[] getDisconnectClientIDs() {
        return disconnectClientIDs.stream().mapToLong(x -> x).toArray();
    }

    @Override
    protected void receive(long clientID, Object data) {
        receiveCount -= 1;
        received.add(data);
        receivedClientIDs.add(clientID);

        if (replyWithStringLength) {
            try {
                send(clientID, ((String) data).length());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void connect(long clientID) {
        connectCount -= 1;
        connectClientIDs.add(clientID);
    }

    @Override
    protected void disconnect(long clientID) {
        disconnectCount -= 1;
        disconnectClientIDs.add(clientID);
    }
}
