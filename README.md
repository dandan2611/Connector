# Connector

Centralize connections to **databases**, **Redis**, and **RabbitMQ** in your Minecraft server or proxy, and access them through a simple API.

Connector is a multi-platform library for [PaperMC](https://papermc.io/) servers and [Velocity](https://velocitypowered.com/) proxies. It discovers connections from environment variables, loads configuration files, and registers services that other plugins can consume.

**[Javadoc](https://dandan2611.github.io/Connector/)**

---

## Installation

**Requires Java 21.**

Add the Connector dependency to your plugin project:

<details>
<summary>Gradle (Kotlin DSL)</summary>

```kotlin
repositories {
    maven("https://nexus.codinbox.fr/repository/maven-public")
}

dependencies {
    // For Paper plugins
    implementation("fr.codinbox.connector:paper:8.0.0")

    // For Velocity plugins
    implementation("fr.codinbox.connector:velocity:8.0.0")

    // Commons only (interfaces + implementations, no platform code)
    implementation("fr.codinbox.connector:commons:8.0.0")
}
```

</details>

<details>
<summary>Maven</summary>

```xml
<repositories>
    <repository>
        <id>codinbox</id>
        <url>https://nexus.codinbox.fr/repository/maven-public</url>
    </repository>
</repositories>

<dependencies>
    <!-- For Paper plugins -->
    <dependency>
        <groupId>fr.codinbox.connector</groupId>
        <artifactId>paper</artifactId>
        <version>8.0.0</version>
    </dependency>

    <!-- For Velocity plugins -->
    <dependency>
        <groupId>fr.codinbox.connector</groupId>
        <artifactId>velocity</artifactId>
        <version>8.0.0</version>
    </dependency>
</dependencies>
```

</details>

---

## Configuration

Each connector type is configured via **environment variables** that point to configuration files. The general pattern is:

```
CONNECTOR_<TYPE>_<NAME>_CONFIG=/path/to/config.file
CONNECTOR_<TYPE>_<NAME>_EXIT_ON_FAILURE=true
```

- `<NAME>` is a unique identifier for the connection (e.g., `MAIN`, `CACHE`, `EVENTS`).
- `EXIT_ON_FAILURE` defaults to `true` — the server shuts down if initialization fails.

### Database

Uses [HikariCP](https://github.com/brettwooldridge/HikariCP) for connection pooling with the MariaDB JDBC driver.

**Environment variables:**
- `CONNECTOR_DB_<NAME>_CONFIG` — path to a HikariCP `.properties` file
- `CONNECTOR_DB_<NAME>_EXIT_ON_FAILURE` — `true` or `false` (default: `true`)

**Example `database.properties`:**
```properties
driverClassName=org.mariadb.jdbc.Driver
jdbcUrl=jdbc:mariadb://127.0.0.1:3306/mydb
dataSource.user=myuser
dataSource.password=mypassword
dataSource.databaseName=mydb
```

### Redis

Uses [Redisson](https://github.com/redisson/redisson) for Redis connectivity.

**Environment variables:**
- `CONNECTOR_REDIS_<NAME>_CONFIG` — path to a Redisson YAML file (`.yml` or `.yaml`)
- `CONNECTOR_REDIS_<NAME>_EXIT_ON_FAILURE` — `true` or `false` (default: `true`)

**Example `redis.yml`:**
```yaml
codec: !<fr.codinbox.connector.commons.codec.JsonJacksonConnectorCodec> {}
singleServerConfig:
  address: "redis://127.0.0.1:6379"
  connectTimeout: 5000
  connectionMinimumIdleSize: 12
  connectionPoolSize: 64
  idleConnectionTimeout: 10000
  retryAttempts: 5
  retryInterval: 3000
  database: 0
  username: null
  password: null
  clientName: "Connector"
```

### RabbitMQ

Uses the [RabbitMQ Java Client](https://www.rabbitmq.com/java-client.html) with a built-in fixed-size channel pool.

**Environment variables:**
- `CONNECTOR_RABBITMQ_<NAME>_CONFIG` — path to a `.properties` file
- `CONNECTOR_RABBITMQ_<NAME>_EXIT_ON_FAILURE` — `true` or `false` (default: `true`)

**Example `rabbitmq.properties`:**
```properties
host=127.0.0.1
port=5672
username=guest
password=guest
virtualHost=/
ssl=false
channelPoolSize=5
```

**Supported properties:**

| Property | Default | Description |
|---|---|---|
| `host` | `localhost` | Broker hostname |
| `port` | `5672` | Broker port |
| `username` | `guest` | Authentication username |
| `password` | `guest` | Authentication password |
| `virtualHost` | `/` | Virtual host |
| `ssl` | `false` | Enable SSL with default JVM SSLContext |
| `channelPoolSize` | `5` | Fixed channel pool size |

## Usage

### Paper

Services are registered with Bukkit's `ServicesManager`. Quick example:

```java
var redis = getServer().getServicesManager().load(RedisConnectorService.class);
redis.getConnection("MAIN").ifPresent(conn -> {
    RedissonClient client = conn.getClient();
    // use client...
});
```

<details>
<summary>Full example</summary>

```java
import fr.codinbox.connector.commons.redis.RedisConnectorService;
import fr.codinbox.connector.commons.database.DatabaseConnectorService;
import fr.codinbox.connector.commons.rabbitmq.RabbitMQConnectorService;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Redis
        var redisService = getServer().getServicesManager().load(RedisConnectorService.class);
        redisService.getConnection("MAIN").ifPresent(conn -> {
            var client = conn.getClient();
            // Use Redisson client...
        });

        // Database
        var dbService = getServer().getServicesManager().load(DatabaseConnectorService.class);
        dbService.getConnection("MAIN").ifPresent(conn -> {
            try (var sqlConn = conn.getConnection()) {
                // Use JDBC connection...
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // RabbitMQ
        var rabbitService = getServer().getServicesManager().load(RabbitMQConnectorService.class);
        rabbitService.getConnection("MAIN").ifPresent(conn -> {
            try (var channel = conn.borrowChannel()) {
                channel.basicPublish("exchange", "key", null, "hello".getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
}
```

</details>

### Velocity

Services are accessed through the static `Connector` class:

```java
var redis = Connector.getRedisService();
redis.getConnection("MAIN").ifPresent(conn -> {
    RedissonClient client = conn.getClient();
    // use client...
});
```

<details>
<summary>Full example</summary>

```java
import fr.codinbox.connector.velocity.Connector;
import fr.codinbox.connector.commons.redis.RedisConnectorService;
import fr.codinbox.connector.commons.database.DatabaseConnectorService;
import fr.codinbox.connector.commons.rabbitmq.RabbitMQConnectorService;

public class MyVelocityPlugin {

    public void example() {
        // Redis
        RedisConnectorService redis = Connector.getRedisService();
        redis.getConnection("MAIN").ifPresent(conn -> {
            var client = conn.getClient();
            // Use Redisson client...
        });

        // Database
        DatabaseConnectorService db = Connector.getDatabaseService();
        db.getConnection("MAIN").ifPresent(conn -> {
            try (var sqlConn = conn.getConnection()) {
                // Use JDBC connection...
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // RabbitMQ
        RabbitMQConnectorService rabbit = Connector.getRabbitMQService();
        rabbit.getConnection("MAIN").ifPresent(conn -> {
            try (var channel = conn.borrowChannel()) {
                channel.basicPublish("exchange", "key", null, "hello".getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
}
```

</details>

---

## Migration from 6.x

**Breaking changes in 7.0.0:**

- **Java 21 required** — Connector 7.0.0 requires Java 21. Update your build toolchain and server runtime.
- **Shadow relocations** — All dependencies (Redisson, HikariCP, MariaDB, RabbitMQ, Jackson) are now relocated under `fr.codinbox.connector.libs.*` in the platform JARs. Downstream plugins using the shadow JAR will see relocated types.
- **New `ConnectionType` values** — The `ConnectionType` enum now includes `RABBITMQ` in addition to `REDIS` and `DATABASE`.
- **Bug fix** — `DatabaseConnectorServiceImpl` previously threw `RedisConnectionException` on failure; it now correctly throws `ConnectionInitException`.
- **Project renamed** — The root project is now named `Connector` (was `RedisConnector`).

---

## License

See [LICENSE](LICENSE) for details.
