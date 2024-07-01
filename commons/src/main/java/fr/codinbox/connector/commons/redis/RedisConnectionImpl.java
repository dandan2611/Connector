package fr.codinbox.connector.commons.redis;

import fr.codinbox.connector.commons.utils.ConnectionType;
import fr.codinbox.connector.commons.exception.ConnectionInitException;
import fr.codinbox.connector.commons.utils.EnvUtils;
import fr.codinbox.connector.commons.utils.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;

public class RedisConnectionImpl implements RedisConnection {

    private final @NotNull Logger logger;

    private final @NotNull String id;

    private final @NotNull String configFilePath;

    private @Nullable RedissonClient client;

    public RedisConnectionImpl(final @NotNull Logger logger,
                               final @NotNull String id,
                               final @NotNull String configFilePath) {
        this.logger = logger;
        this.id = id;
        this.configFilePath = configFilePath;
    }

    @Override
    public void init() throws ConnectionInitException {
        final var extension = FileUtils.getExtension(this.configFilePath);

        if (extension == null)
            throw new ConnectionInitException("Invalid config file extension: '" + this.configFilePath + "'");

        Config config = null;
        try {
            if (extension.equalsIgnoreCase("yml") || extension.equalsIgnoreCase("yaml")) {
                config = Config.fromYAML(new File(this.configFilePath));
            } else {
                throw new ConnectionInitException("Invalid file extension: '" + this.configFilePath + "', only 'yml' and 'yaml' are supported");
            }
        } catch (Exception e) {
            throw new ConnectionInitException(e);
        }

        if (config == null)
            throw new ConnectionInitException("Invalid configuration file: '" + this.configFilePath + "'");

        logger.fine(id + ": Connecting to Redis connection");
        this.client = Redisson.create(config);
    }

    @Override
    public void shutdown() {
        if (this.client != null)
            this.client.shutdown();
    }

    @Override
    public @NotNull RedissonClient getClient() {
        return Objects.requireNonNull(this.client);
    }

    @Override
    public boolean isExitOnFailure() {
        return EnvUtils.isExitOnFailure(ConnectionType.REDIS, this.id);
    }

}
