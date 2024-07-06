# Connector

Easily centralize connections to databases and Redis in your server, and access them through a blazingly simple api.

---

## Installation

Download latest release from [here](https://nexus.codinbox.fr/#browse/browse:maven-public:fr%2Fcodinbox%2Fredisconnector) corresponding to your platform

```Kotlin
repositories {
    maven("https://nexus.codinbox.fr/repository/maven-public")
}

dependencies {
    implementation("fr.codinbox.connector:paper:version")
    implementation("fr.codinbox.connector:velocity:version")
}
```

## Config

### Databases

Connector connects to databases using HikariCP. Connector can be configured using the following environment variables:

- `CONNECTOR_DB_<NAME>_CONFIG=file1.properties` : A file to load the database configuration from. HikariCP configuration documentation can be found [here](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- `CONNECTOR_DB_<NAME>_EXIT_ON_FAILURE=true` : If the connection fails, the server will exit immediately. Default is `true`.

Example simple configuration file:

```properties
driverClassName=org.mariadb.jdbc.Driver
jdbcUrl=jdbc:mariadb://IP:PORT/yourdb
dataSource.user=youruserhere
dataSource.password=hypersecretpassword
dataSource.databaseName=superduperdatabase
```

### Redis

Connector can be configured using the following environment variables:

- `CONNECTOR_REDIS_<NAME>_CONFIG=file1.yml` : A file to load the redis configuration from. Redisson configuration documentation can be found [here](https://github.com/redisson/redisson/wiki/2.-Configuration). The file extension must be `.yml` or `.yaml`. `<NAME>` is the name of the connection, and must be unique.
- `CONNECTOR_REDIS_<NAME>_EXIT_ON_FAILURE=true` : If the connection fails, the server will exit immediately. Default is `true`.

Example simple configuration file:

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
  clientName: "RedisConnector"
```