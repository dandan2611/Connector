package fr.codinbox.redisconnector;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.codinbox.redisconnector.connector.redis.RedisConnectorServiceImpl;
import fr.codinbox.redisconnector.connector.exception.ConnectionInitException;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

@Plugin(
        id = "redisconnector",
        name = "redisconnector",
        version = "4.0.1"
)
public class RedisConnectorPlugin {

    private final @NotNull Logger logger;
    private final @NotNull ProxyServer server;

    public @Inject RedisConnectorPlugin(final @NotNull Logger logger, final @NotNull ProxyServer server) {
        this.logger = logger;
        this.server = server;

        final var serviceImpl = new RedisConnectorServiceImpl(this.logger);

        try {
            serviceImpl.init();
            RedisConnector.setService(serviceImpl);
        } catch (ConnectionInitException exception) {
            this.logger.severe("Failed to initialize RedisConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            this.server.shutdown();
            return;
        }
    }

    @Subscribe
    private void onProxyShutdown(final @NotNull ProxyShutdownEvent event) {
        final var service = RedisConnector.getService();
        service.shutdown();
    }

}
