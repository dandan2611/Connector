package fr.codinbox.redisconnector.connector;

import org.jetbrains.annotations.NotNull;
import org.redisson.client.RedisConnectionException;
import fr.codinbox.redisconnector.connector.exception.ConnectionInitException;
import fr.codinbox.redisconnector.utils.EnvUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisConnectorServiceImpl implements RedisConnectorService {

    private final @NotNull Logger logger;

    private final @NotNull Map<String, RedisConnection> redisConnectionMap;

    public RedisConnectorServiceImpl(final @NotNull Logger logger) {
        this.logger = logger;
        this.redisConnectionMap = new HashMap<>();
    }

    @Override
    public void init() throws ConnectionInitException {
        // Connect to redis servers
        final var idList = EnvUtils.getEnvironmentIds();
        this.logger.info(String.format("%d connection(s) to initialize", idList.size()));
        for (String id : idList) {
            final var configFilepath = EnvUtils.getConfigFilepath(id);

            if (configFilepath == null) {
                logger.warning("No configuration file found for id: " + id);
                if (EnvUtils.isExitOnFailure(id)) {
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
                if (EnvUtils.isExitOnFailure(id)) {
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
