package fr.codinbox.connector.commons.database;

import fr.codinbox.connector.commons.exception.ConnectionInitException;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

/**
 * Represents a managed database connection backed by HikariCP.
 *
 * <p>Connections are configured via {@code .properties} files referenced by the
 * {@code CONNECTOR_DB_<ID>_CONFIG} environment variable. The properties format
 * follows the HikariCP configuration specification.</p>
 *
 * @see DatabaseConnectorService
 */
public interface DatabaseConnection {

    /**
     * Returns a JDBC connection from the underlying HikariCP pool.
     *
     * @return a {@link Connection} from the pool
     * @throws RuntimeException wrapping {@link java.sql.SQLException} if the connection cannot be obtained
     */
    @NotNull Connection getConnection();

    /**
     * Initializes the connection pool by loading HikariCP configuration.
     *
     * @throws ConnectionInitException if the configuration is invalid or the pool cannot be created
     */
    void init() throws ConnectionInitException;

    /**
     * Shuts down the HikariCP connection pool and releases all associated resources.
     */
    void shutdown();

    /**
     * Returns whether this connection is configured to cause server shutdown on initialization failure.
     *
     * @return {@code true} if the server should exit when this connection fails to initialize
     */
    boolean isExitOnFailure();
}
