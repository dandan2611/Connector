package fr.codinbox.connector.commons.codec;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.codec.JsonJacksonCodec;

/**
 * Extended Jackson codec for Redisson that registers support for Java 8+ types.
 *
 * <p>This codec extends Redisson's default {@link JsonJacksonCodec} by registering:</p>
 * <ul>
 *   <li>{@link JavaTimeModule} — serialization support for {@code java.time} types
 *       (e.g., {@code Instant}, {@code LocalDateTime})</li>
 *   <li>{@link Jdk8Module} — serialization support for {@code Optional} and other JDK 8 types</li>
 * </ul>
 *
 * <p>To use this codec in a Redisson YAML configuration:</p>
 * <pre>{@code
 * codec: !<fr.codinbox.connector.commons.codec.JsonJacksonConnectorCodec> {}
 * }</pre>
 *
 * @see JsonJacksonCodec
 */
public class JsonJacksonConnectorCodec extends JsonJacksonCodec {

    /**
     * Creates a new codec with Java 8 time and Optional support registered.
     */
    public JsonJacksonConnectorCodec() {
        super();

        final var mapper = super.getObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
    }
}
