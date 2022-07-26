package jdtp;

public class JDTPException extends Exception {
    public JDTPException() {}

    public JDTPException(String message) {
        super(message);
    }

    public JDTPException(String message, Exception innerException) {
        super(message, innerException);
    }
}
