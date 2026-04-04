package fr.codinbox.connector.commons.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A pooled RabbitMQ channel wrapper that implements {@link AutoCloseable} to automatically
 * return the channel to the pool when closed.
 *
 * <p>This class provides both raw channel access and convenience methods for common
 * RabbitMQ operations. It should always be used within a try-with-resources block:</p>
 * <pre>{@code
 * try (PooledChannel ch = connection.borrowChannel()) {
 *     ch.basicPublish("exchange", "routingKey", null, "hello".getBytes());
 * }
 * }</pre>
 *
 * <p>Closing this wrapper does <em>not</em> close the underlying channel; it returns it
 * to the pool for reuse.</p>
 *
 * @see RabbitMQConnection#borrowChannel()
 */
public class PooledChannel implements AutoCloseable {

    private final @NotNull Channel channel;
    private final @NotNull java.util.concurrent.BlockingQueue<Channel> pool;

    /**
     * Creates a new pooled channel wrapper.
     *
     * @param channel the underlying RabbitMQ channel
     * @param pool    the pool to return the channel to on {@link #close()}
     */
    PooledChannel(final @NotNull Channel channel,
                  final @NotNull java.util.concurrent.BlockingQueue<Channel> pool) {
        this.channel = channel;
        this.pool = pool;
    }

    /**
     * Returns the underlying RabbitMQ {@link Channel} for direct access.
     *
     * @return the raw channel
     */
    public @NotNull Channel getChannel() {
        return this.channel;
    }

    /**
     * Publishes a message to the specified exchange with the given routing key.
     *
     * @param exchange   the exchange to publish to
     * @param routingKey the routing key
     * @param props      message properties, may be {@code null}
     * @param body       the message body
     * @throws IOException if an I/O error occurs during publishing
     * @see Channel#basicPublish(String, String, AMQP.BasicProperties, byte[])
     */
    public void basicPublish(final @NotNull String exchange,
                             final @NotNull String routingKey,
                             final @Nullable AMQP.BasicProperties props,
                             final byte @NotNull [] body) throws IOException {
        this.channel.basicPublish(exchange, routingKey, props, body);
    }

    /**
     * Starts a non-auto-acknowledged consumer on the specified queue using a callback.
     *
     * @param queue    the queue to consume from
     * @param callback the callback to invoke for each delivered message
     * @return the consumer tag
     * @throws IOException if an I/O error occurs
     * @see Channel#basicConsume(String, boolean, DeliverCallback, com.rabbitmq.client.CancelCallback)
     */
    public @NotNull String basicConsume(final @NotNull String queue,
                                        final @NotNull DeliverCallback callback) throws IOException {
        return this.channel.basicConsume(queue, false, callback, consumerTag -> {});
    }

    /**
     * Starts a non-auto-acknowledged consumer that delivers messages to a {@link BlockingQueue}.
     *
     * <p>Callers can poll or take from the returned queue to receive messages synchronously:</p>
     * <pre>{@code
     * BlockingQueue<Delivery> deliveries = channel.consume("my-queue");
     * Delivery delivery = deliveries.take(); // blocks until a message arrives
     * }</pre>
     *
     * @param queue the queue to consume from
     * @return a blocking queue that receives deliveries
     * @throws IOException if an I/O error occurs
     */
    public @NotNull BlockingQueue<Delivery> consume(final @NotNull String queue) throws IOException {
        final var deliveries = new ArrayBlockingQueue<Delivery>(256);
        this.channel.basicConsume(queue, false,
                (consumerTag, delivery) -> {
                    try {
                        deliveries.put(delivery);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                },
                consumerTag -> {});
        return deliveries;
    }

    /**
     * Returns the channel to the pool. Does <em>not</em> close the underlying channel.
     *
     * <p>If the channel is no longer open, it is discarded rather than returned to the pool.</p>
     */
    @Override
    public void close() {
        if (this.channel.isOpen()) {
            this.pool.offer(this.channel);
        }
    }
}
