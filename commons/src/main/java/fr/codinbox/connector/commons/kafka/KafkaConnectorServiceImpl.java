package fr.codinbox.connector.commons.kafka;

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
 * Default implementation of {@link KafkaConnectorService} that discovers and manages
 * Kafka connections from environment variables.
 *
 * @see KafkaConnectorService
 * @see KafkaConnectionImpl
 */
public class KafkaConnectorServiceImpl implements KafkaConnectorService {

    private static final @NotNull ConnectionType CONNECTION_TYPE = ConnectionType.KAFKA;

    private final @NotNull Logger logger;
    private final @NotNull Map<String, KafkaConnection> connectionMap;

    /**
     * Creates a new Kafka connector service.
     *
     * @param logger the logger for diagnostic messages
     */
    public KafkaConnectorServiceImpl(final @NotNull Logger logger) {
        this.logger = logger;
        this.connectionMap = new HashMap<>();
    }

    @Override
    public void init() throws ConnectionInitException {
        final var idList = EnvUtils.getEnvironmentIds(CONNECTION_TYPE);
        this.logger.info(String.format("%d Kafka connection(s) to initialize", idList.size()));
        for (String id : idList) {
            final var configFilepath = EnvUtils.getConfigFilepath(CONNECTION_TYPE, id);

            if (configFilepath == null) {
                logger.warning("No configuration file found for Kafka id: " + id);
                if (EnvUtils.isExitOnFailure(CONNECTION_TYPE, id)) {
                    throw new ConnectionInitException("No configuration file found for Kafka id: " + id);
                }
                continue;
            }

            try {
                this.logger.info(id + ": Creating Kafka connection object");
                final var connection = new KafkaConnectionImpl(this.logger, id, configFilepath);
                connection.init();
                this.connectionMap.put(id, connection);
            } catch (Exception exception) {
                logger.log(Level.SEVERE, "Failed to create Kafka connection for id: " + id, exception);
                if (EnvUtils.isExitOnFailure(CONNECTION_TYPE, id)) {
                    throw new ConnectionInitException("Failed to create Kafka connection for id: " + id);
                }
            }
        }
    }

    @Override
    public void shutdown() {
        for (KafkaConnection value : this.connectionMap.values())
            value.shutdown();
    }

    @Override
    public @NotNull Optional<KafkaConnection> getConnection(@NotNull String id) {
        return Optional.ofNullable(this.connectionMap.get(id));
    }
}
