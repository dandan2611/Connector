package fr.codinbox.connector.commons.exception;

/**
 * Exception thrown when a connector connection fails to initialize.
 *
 * <p>This exception is used consistently across all connector types (Redis, Database,
 * RabbitMQ) to signal initialization failures. When a connection is configured
 * with exit-on-failure enabled, this exception causes the server to shut down.</p>
 *
 * @see fr.codinbox.connector.commons.utils.EnvUtils#isExitOnFailure
 */
public class ConnectionInitException extends Exception {

    /**
     * Creates a new exception with the specified detail message.
     *
     * @param message the detail message describing the initialization failure
     */
    public ConnectionInitException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified cause.
     *
     * @param cause the underlying cause of the initialization failure
     */
    public ConnectionInitException(Throwable cause) {
        super(cause);
    }
}
