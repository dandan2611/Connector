package fr.codinbox.connector.commons.exception;

public class ConnectionInitException extends Exception {

    public ConnectionInitException(String message) {
        super(message);
    }

    public ConnectionInitException(Throwable cause) {
        super(cause);
    }

}
