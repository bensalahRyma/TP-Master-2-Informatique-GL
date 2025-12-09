package org.example.jdm;

public class JdmApiException extends RuntimeException {

    public JdmApiException(String message) {
        super(message);
    }

    public JdmApiException(String message, Throwable cause) {
        super(message, cause);
    }
}