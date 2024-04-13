# RedisConnector

Easily centralize connections to Redis in your server, and access them through a blazingly simple api.

---

## Installation

SOON

## Config

RedisConnector can be configured using the following environment variables:

- `CONNECTOR_REDIS_<NAME>_CONFIG=file1,file2,file3` : A file to load the redis configuration from. Redisson configuration documentation can be found [here](https://github.com/redisson/redisson/wiki/2.-Configuration). The file extension must be `.yml` or `.yaml`. `<NAME>` is the name of the connection, and must be unique.
- `CONNECTOR_REDIS_<NAME>_EXIT_ON_FAILURE=true` : If the connection fails, the server will exit immediately. Default is `true`.

Example simple configuration file:

```yaml
codec: !<org.redisson.codec.JsonJacksonCodec> {}
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