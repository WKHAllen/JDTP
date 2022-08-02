package jdtp;

class TestClient extends Client {
    public boolean receivingRandomMessage = false;
    public long randomMessage;

    @Override
    protected void receive(Object data) {
        if (!receivingRandomMessage) {
            String message = (String) data;
            System.out.printf("[CLIENT] Received data from server: %s (size %d)\n", message, message.length());
        } else {
            long dataLong = (long) data;
            System.out.printf("[CLIENT] Received large random message from server (size %d, %d)\n", dataLong, randomMessage);
            assert dataLong == randomMessage;
        }
    }

    @Override
    protected void disconnected() {
        System.out.println("[CLIENT] Unexpectedly disconnected from server");
    }
}
