package fr.codinbox.connector.commons.utils;

/**
 * Enumerates the supported connection types managed by the Connector library.
 *
 * <p>Each value corresponds to a specific backing technology and determines the
 * environment variable prefix used for configuration discovery.</p>
 *
 * @see EnvUtils
 */
public enum ConnectionType {

    /**
     * Redis connection type, backed by Redisson.
     */
    REDIS,

    /**
     * Database connection type, backed by HikariCP with MariaDB.
     */
    DATABASE,

    /**
     * RabbitMQ connection type, backed by the RabbitMQ AMQP client.
     */
    RABBITMQ,

    /**
     * Kafka connection type, backed by the Apache Kafka clients library.
     */
    KAFKA,
}
