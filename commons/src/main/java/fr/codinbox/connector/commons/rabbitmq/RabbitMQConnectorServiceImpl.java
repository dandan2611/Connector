package fr.codinbox.connector.commons.rabbitmq;

import fr.codinbox.connector.commons.exception.ConnectionInitException;
import fr.codinbox.connector.commons.utils.ConnectionType;
import fr.codinbox.connector.commons.utils.EnvUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of {@link RabbitMQConnectorService} that discovers and manages
 * RabbitMQ connections from environment variables.
 *
 * @see RabbitMQConnectorService
 * @see RabbitMQConnectionImpl
 */
public class RabbitMQConnectorServiceImpl implements RabbitMQConnectorService {

    private static final @NotNull ConnectionType CONNECTION_TYPE = ConnectionType.RABBITMQ;

    private final @NotNull Logger logger;
    private final @NotNull Map<String, RabbitMQConnection> connectionMap;

    /**
     * Creates a new RabbitMQ connector service.
     *
     * @param logger the logger for diagnostic messages
     */
    public RabbitMQConnectorServiceImpl(final @NotNull Logger logger) {
        this.logger = logger;
        this.connectionMap = new HashMap<>();
    }

    @Override
    public void init() throws ConnectionInitException {
        final var idList = EnvUtils.getEnvironmentIds(CONNECTION_TYPE);
        this.logger.info(String.format("%d RabbitMQ connection(s) to initialize", idList.size()));
        for (String id : idList) {
            final var configFilepath = EnvUtils.getConfigFilepath(CONNECTION_TYPE, id);

            if (configFilepath == null) {
                logger.warning("No configuration file found for RabbitMQ id: " + id);
                if (EnvUtils.isExitOnFailure(CONNECTION_TYPE, id)) {
                    throw new ConnectionInitException("No configuration file found for RabbitMQ id: " + id);
                }
                continue;
            }

            try {
                this.logger.info(id + ": Creating RabbitMQ connection object");
                final var connection = new RabbitMQConnectionImpl(this.logger, id, configFilepath);
                connection.init();
                this.connectionMap.put(id, connection);
            } catch (Exception exception) {
                logger.log(Level.SEVERE, "Failed to create RabbitMQ connection for id: " + id, exception);
                if (EnvUtils.isExitOnFailure(CONNECTION_TYPE, id)) {
                    throw new ConnectionInitException("Failed to create RabbitMQ connection for id: " + id);
                }
            }
        }
    }

    @Override
    public void shutdown() {
        for (RabbitMQConnection value : this.connectionMap.values())
            value.shutdown();
    }

    @Override
    public @NotNull Optional<RabbitMQConnection> getConnection(@NotNull String id) {
        return Optional.ofNullable(this.connectionMap.get(id));
    }
}
