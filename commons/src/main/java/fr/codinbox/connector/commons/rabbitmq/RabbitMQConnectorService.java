package fr.codinbox.connector.commons.rabbitmq;

import fr.codinbox.connector.commons.exception.ConnectionInitException;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Service interface for managing multiple named RabbitMQ connections.
 *
 * <p>Connections are discovered from environment variables with the
 * {@code CONNECTOR_RABBITMQ_} prefix. Each connection is identified by a unique name
 * extracted from the environment variable pattern
 * {@code CONNECTOR_RABBITMQ_<NAME>_CONFIG}.</p>
 *
 * <p><b>Logging contract:</b></p>
 * <ul>
 *   <li>{@code INFO} — lifecycle events (initialization start, connection count, connection created)</li>
 *   <li>{@code WARNING} — recoverable failures (missing config when exit-on-failure is disabled)</li>
 *   <li>{@code SEVERE} — fatal failures that will cause server shutdown</li>
 * </ul>
 *
 * @see RabbitMQConnection
 */
public interface RabbitMQConnectorService {

    /**
     * Initializes all RabbitMQ connections discovered from environment variables.
     *
     * @throws ConnectionInitException if a connection with exit-on-failure enabled fails to initialize
     */
    void init() throws ConnectionInitException;

    /**
     * Shuts down all managed RabbitMQ connections, closing channels and connections.
     */
    void shutdown();

    /**
     * Retrieves a RabbitMQ connection by its identifier.
     *
     * @param id the connection identifier (case-sensitive, as extracted from environment variables)
     * @return an {@link Optional} containing the connection, or empty if no connection exists with that id
     */
    @NotNull Optional<RabbitMQConnection> getConnection(final @NotNull String id);
}
