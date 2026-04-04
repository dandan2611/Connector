package fr.codinbox.connector.paper;

import fr.codinbox.connector.commons.database.DatabaseConnectorService;
import fr.codinbox.connector.commons.database.DatabaseConnectorServiceImpl;
import fr.codinbox.connector.commons.exception.ConnectionInitException;
import fr.codinbox.connector.commons.kafka.KafkaConnectorService;
import fr.codinbox.connector.commons.kafka.KafkaConnectorServiceImpl;
import fr.codinbox.connector.commons.rabbitmq.RabbitMQConnectorService;
import fr.codinbox.connector.commons.rabbitmq.RabbitMQConnectorServiceImpl;
import fr.codinbox.connector.commons.redis.RedisConnectorService;
import fr.codinbox.connector.commons.redis.RedisConnectorServiceImpl;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * PaperMC plugin entry point for the Connector library.
 *
 * <p>This plugin initializes and registers all four connector services
 * (Database, Redis, RabbitMQ, Kafka) with Bukkit's {@link org.bukkit.plugin.ServicesManager}.
 * Other plugins can retrieve connector services via:</p>
 * <pre>{@code
 * var redisService = getServer().getServicesManager().load(RedisConnectorService.class);
 * var dbService = getServer().getServicesManager().load(DatabaseConnectorService.class);
 * var rabbitService = getServer().getServicesManager().load(RabbitMQConnectorService.class);
 * var kafkaService = getServer().getServicesManager().load(KafkaConnectorService.class);
 * }</pre>
 *
 * <p>Initialization order: Database, Redis, RabbitMQ, Kafka. If any connector with
 * exit-on-failure enabled fails, the server is shut down immediately.</p>
 *
 * @see DatabaseConnectorService
 * @see RedisConnectorService
 * @see RabbitMQConnectorService
 * @see KafkaConnectorService
 */
public class ConnectorPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Database
        final var databaseServiceImpl = new DatabaseConnectorServiceImpl(super.getLogger());
        try {
            databaseServiceImpl.init();
        } catch (ConnectionInitException exception) {
            super.getLogger().severe("Failed to initialize DatabaseConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            super.getServer().shutdown();
            return;
        }
        super.getServer().getServicesManager().register(
                DatabaseConnectorService.class,
                databaseServiceImpl,
                this,
                ServicePriority.Normal
        );

        // Redis
        final var redisServiceImpl = new RedisConnectorServiceImpl(super.getLogger());
        try {
            redisServiceImpl.init();
        } catch (ConnectionInitException exception) {
            super.getLogger().severe("Failed to initialize RedisConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            super.getServer().shutdown();
            return;
        }
        super.getServer().getServicesManager().register(
                RedisConnectorService.class,
                redisServiceImpl,
                this,
                ServicePriority.Normal
        );

        // RabbitMQ
        final var rabbitMQServiceImpl = new RabbitMQConnectorServiceImpl(super.getLogger());
        try {
            rabbitMQServiceImpl.init();
        } catch (ConnectionInitException exception) {
            super.getLogger().severe("Failed to initialize RabbitMQConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            super.getServer().shutdown();
            return;
        }
        super.getServer().getServicesManager().register(
                RabbitMQConnectorService.class,
                rabbitMQServiceImpl,
                this,
                ServicePriority.Normal
        );

        // Kafka
        final var kafkaServiceImpl = new KafkaConnectorServiceImpl(super.getLogger());
        try {
            kafkaServiceImpl.init();
        } catch (ConnectionInitException exception) {
            super.getLogger().severe("Failed to initialize KafkaConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            super.getServer().shutdown();
            return;
        }
        super.getServer().getServicesManager().register(
                KafkaConnectorService.class,
                kafkaServiceImpl,
                this,
                ServicePriority.Normal
        );
    }

    @Override
    public void onDisable() {
        final var kafkaService = super.getServer().getServicesManager().load(KafkaConnectorService.class);
        if (kafkaService != null) {
            kafkaService.shutdown();
        }

        final var rabbitMQService = super.getServer().getServicesManager().load(RabbitMQConnectorService.class);
        if (rabbitMQService != null) {
            rabbitMQService.shutdown();
        }

        final var redisService = super.getServer().getServicesManager().load(RedisConnectorService.class);
        if (redisService != null) {
            redisService.shutdown();
        }

        final var databaseService = super.getServer().getServicesManager().load(DatabaseConnectorService.class);
        if (databaseService != null) {
            databaseService.shutdown();
        }

        super.getServer().getServicesManager().unregisterAll(this);
    }
}
