package jdtp;

import java.io.*;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * JDTP utilities.
 */
class Util {
    /**
     * The length of the size portion of each message.
     */
    public static final int lenSize = 5;

    /**
     * The default port.
     */
    public static final int defaultPort = 29275;

    /**
     * The server listen backlog.
     */
    public static final int listenBacklog = 8;

    /**
     * Get the default host.
     *
     * @return The default host.
     * @throws UnknownHostException When the default host cannot be retrieved.
     */
    public static String defaultHost() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

    /**
     * Wrap a <code>byte[]</code> as a <code>Byte[]</code>.
     *
     * @param array The primitive byte array.
     * @return The resulting byte array.
     */
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

    /**
     * Unwrap a <code>Byte[]</code> as a <code>byte[]</code>.
     *
     * @param array The byte array.
     * @return The primitive byte array.
     */
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

    /**
     * Concatenate two arrays.
     *
     * @param a   The first array.
     * @param b   The second array.
     * @param <T> The array type.
     * @return The resulting concatenated array.
     */
    private static <T> T[] arrayConcat(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    /**
     * Serialize an object to bytes.
     *
     * @param obj The object to serialize.
     * @return The resulting bytes.
     * @throws IOException If an error occurs while serializing.
     */
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(obj);
        out.flush();

        byte[] bytes = bos.toByteArray();

        bos.close();

        return bytes;
    }

    /**
     * Deserialize an object from bytes.
     *
     * @param bytes The bytes to deserialize.
     * @return The resulting object.
     * @throws IOException            If an error occurs while deserializing.
     * @throws ClassNotFoundException If the object cannot be constructed.
     */
    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = new ObjectInputStream(bis);

        Object obj = in.readObject();

        in.close();

        return obj;
    }

    /**
     * Encode the size portion of a message to bytes.
     *
     * @param size The message size.
     * @return The encoded message size.
     */
    public static byte[] encodeMessageSize(long size) {
        byte[] encodedMessageSize = new byte[lenSize];

        for (int i = lenSize - 1; i >= 0; i--) {
            encodedMessageSize[i] = (byte) (size & 0xff);
            size >>= 8;
        }

        return encodedMessageSize;
    }

    /**
     * Decode the size portion of a message.
     *
     * @param encodedMessageSize The encoded message size.
     * @return The actual message size.
     */
    public static long decodeMessageSize(byte[] encodedMessageSize) {
        long size = 0;

        for (int i = 0; i < lenSize; i++) {
            size <<= 8;
            size += encodedMessageSize[i] & 0xff;
        }

        return size;
    }

    /**
     * Encode a message.
     *
     * @param data The message data.
     * @return The encoded message.
     */
    public static byte[] encodeMessage(byte[] data) {
        byte[] encodedMessageSize = encodeMessageSize(data.length);
        return unwrapByteArray(arrayConcat(wrapByteArray(encodedMessageSize), wrapByteArray(data)));
    }

    /**
     * Decode a message.
     *
     * @param encodedMessage The encoded message.
     * @return The decoded message data.
     */
    public static byte[] decodeMessage(byte[] encodedMessage) {
        return Arrays.copyOfRange(encodedMessage, lenSize, encodedMessage.length);
    }
}
