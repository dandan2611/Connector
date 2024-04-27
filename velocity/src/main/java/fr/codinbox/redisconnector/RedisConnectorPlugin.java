package fr.codinbox.redisconnector;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.codinbox.redisconnector.connector.RedisConnectorServiceImpl;
import fr.codinbox.redisconnector.connector.exception.ConnectionInitException;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

@Plugin(
        id = "redisconnector",
        name = "redisconnector",
        version = "4.0.0"
)
public class RedisConnectorPlugin {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;

    @Subscribe
    private void onProxyInit(final @NotNull ProxyInitializeEvent event) {
        final var serviceImpl = new RedisConnectorServiceImpl(this.logger);

        try {
            serviceImpl.init();
        } catch (ConnectionInitException exception) {
            this.logger.severe("Failed to initialize RedisConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            this.server.shutdown();
            return;
        }

        RedisConnector.setService(serviceImpl);
    }

    @Subscribe
    private void onProxyShutdown(final @NotNull ProxyShutdownEvent event) {
        final var service = RedisConnector.getService();
        service.shutdown();
    }

}
