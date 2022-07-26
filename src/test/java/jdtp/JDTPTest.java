package jdtp;

import org.junit.jupiter.api.Test;

public class JDTPTest {
    @Test void doNothing() {
        TestServer server = new TestServer();
        TestClient client = new TestClient();
        System.out.println("Testing");
    }
}
