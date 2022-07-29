package jdtp;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

class Util {
    public static final int lenSize = 5;
    public static final int defaultPort = 29275;
    public static final int listenBacklog = 8;

    public static String defaultHost() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

    private static Byte[] wrapByteArray(byte[] array) {
        if (array == null) {
            return null;
        }

        final Byte[] result = new Byte[array.length];

        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }

        return result;
    }

    private static byte[] unwrapByteArray(Byte[] array) {
        if (array == null) {
            return null;
        }

        final byte[] result = new byte[array.length];

        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }

        return result;
    }

    private static <T> T[] arrayConcat(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    public static byte[] encodeMessageSize(long size) {
        byte[] encodedMessageSize = new byte[lenSize];

        for (int i = lenSize - 1; i >= 0; i--) {
            encodedMessageSize[i] = (byte) (size & 0xff);
            size >>= 8;
        }

        return encodedMessageSize;
    }

    public static long decodeMessageSize(byte[] encodedMessageSize) {
        long size = 0;

        for (int i = 0; i < lenSize; i++) {
            size <<= 8;
            size += encodedMessageSize[i];
        }

        return size;
    }

    public static byte[] encodeMessage(byte[] data) {
        byte[] encodedMessageSize = encodeMessageSize(data.length);
        byte[] encodedMessage = unwrapByteArray(arrayConcat(wrapByteArray(encodedMessageSize), wrapByteArray(data)));
        return encodedMessage;
    }

    public static byte[] decodeMessage(byte[] encodedMessage) {
        byte[] data = Arrays.copyOfRange(encodedMessage, lenSize, encodedMessage.length);
        return data;
    }
}
