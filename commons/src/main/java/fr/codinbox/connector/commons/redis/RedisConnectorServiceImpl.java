package fr.codinbox.connector.commons.redis;

import fr.codinbox.connector.commons.utils.ConnectionType;
import fr.codinbox.connector.commons.exception.ConnectionInitException;
import fr.codinbox.connector.commons.utils.EnvUtils;
import org.jetbrains.annotations.NotNull;
import org.redisson.client.RedisConnectionException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisConnectorServiceImpl implements RedisConnectorService {

    private static final @NotNull ConnectionType CONNECTION_TYPE = ConnectionType.REDIS;

    private final @NotNull Logger logger;

    private final @NotNull Map<String, RedisConnection> redisConnectionMap;

    public RedisConnectorServiceImpl(final @NotNull Logger logger) {
        this.logger = logger;
        this.redisConnectionMap = new HashMap<>();
    }

    @Override
    public void init() throws ConnectionInitException {
        // Connect to redis servers
        final var idList = EnvUtils.getEnvironmentIds(CONNECTION_TYPE);
        this.logger.info(String.format("%d connection(s) to initialize", idList.size()));
        for (String id : idList) {
            final var configFilepath = EnvUtils.getConfigFilepath(CONNECTION_TYPE, id);

            if (configFilepath == null) {
                logger.warning("No configuration file found for id: " + id);
                if (EnvUtils.isExitOnFailure(CONNECTION_TYPE, id)) {
                    throw new ConnectionInitException("No configuration file found for id: " + id);
                }
                continue;
            }

            try {
                this.logger.info(id + ": Creating connection object");
                final var connection = new RedisConnectionImpl(this.logger, id, configFilepath);
                connection.init();
                this.redisConnectionMap.put(id, connection);
            } catch (Exception exception) {
                logger.log(Level.SEVERE, "Failed to create connection for id: " + id, exception);
                if (EnvUtils.isExitOnFailure(CONNECTION_TYPE, id)) {
                    throw new RedisConnectionException("Failed to create connection for id: " + id, exception);
                }
            }
        }
    }

    @Override
    public void shutdown() {
        for (RedisConnection value : this.redisConnectionMap.values())
            value.shutdown();
    }

    @Override
    public @NotNull Optional<RedisConnection> getConnection(@NotNull String id) {
        return Optional.ofNullable(this.redisConnectionMap.get(id));
    }

}
