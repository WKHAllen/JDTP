package jdtp;

import java.net.InetAddress;
import java.net.UnknownHostException;

class Util {
    public static final int lenSize = 5;
    public static final int defaultPort = 29275;
    public static final int listenBacklog = 8;

    public static String defaultHost() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }
}
