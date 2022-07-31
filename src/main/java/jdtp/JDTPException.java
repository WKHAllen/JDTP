package jdtp;

/**
 * A JDTP exception.
 */
public class JDTPException extends Exception {
    /**
     * Instantiate a JDTP exception.
     */
    public JDTPException() {
    }

    /**
     * Instantiate a JDTP exception with an error message.
     *
     * @param message The error message.
     */
    public JDTPException(String message) {
        super(message);
    }

    /**
     * Instantiate a JDTP exception with an error message and an inner exception.
     *
     * @param message        The error message.
     * @param innerException The inner exception.
     */
    public JDTPException(String message, Exception innerException) {
        super(message, innerException);
    }
}
