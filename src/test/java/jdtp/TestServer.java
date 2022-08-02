package jdtp;

class TestServer extends Server {
    public boolean receivingRandomMessage = false;
    public long randomMessage;

    @Override
    protected void receive(long clientID, Object data) {
        if (!receivingRandomMessage) {
            String message = (String) data;
            System.out.printf("[SERVER] Received data from client #%d: %s (size %d)\n", clientID, message, message.length());
        } else {
            long dataLong = (long) data;
            System.out.printf("[SERVER] Received large random message from client (size %d, %d)\n", dataLong, randomMessage);
            assert dataLong == randomMessage;
        }
    }

    @Override
    protected void connect(long clientID) {
        System.out.printf("[SERVER] Client #%d connected\n", clientID);
    }

    @Override
    protected void disconnect(long clientID) {
        System.out.printf("[SERVER] Client #%d disconnected\n", clientID);
    }
}
