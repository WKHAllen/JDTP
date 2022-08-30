package jdtp;

import java.util.ArrayList;

class TestClient extends Client {
    private int receiveCount;
    private int disconnectedCount;
    private final ArrayList<Object> received = new ArrayList<>();

    TestClient(int receiveCount, int disconnectedCount) {
        super();

        this.receiveCount = receiveCount;
        this.disconnectedCount = disconnectedCount;
    }

    public boolean eventsDone() {
        return receiveCount == 0 && disconnectedCount == 0;
    }

    public int getReceiveCount() {
        return receiveCount;
    }

    public int getDisconnectedCount() {
        return disconnectedCount;
    }

    public Object[] getReceived() {
        return received.toArray();
    }

    @Override
    protected void receive(Object data) {
        receiveCount -= 1;
        received.add(data);
    }

    @Override
    protected void disconnected() {
        disconnectedCount -= 1;
    }
}
