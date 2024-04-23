package fr.codinbox.redisconnector.codec;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.codec.JsonJacksonCodec;

public class JsonJacksonConnectorCodec extends JsonJacksonCodec {

    public JsonJacksonConnectorCodec() {
        super();

        final var mapper = super.getObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
    }

}
