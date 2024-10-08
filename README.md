### CDC Service for MySQL
### Prerequisite

##### Install 
- [Docker](script/install_docker.sh)
- [Docker Compose](script/install_docker_compose.sh)

##### MySQL Config
- `log_bin` should be ON
- `binlog_format` should be ROW
- `binlog_row_image` should be FULL
    ```text
    SHOW VARIABLES LIKE 'log_bin';
    SHOW VARIABLES LIKE 'binlog_format';
    SHOW VARIABLES LIKE 'binlog_row_image';
    ```
- `database.user` uses for cdc should have `RELOAD` or `FLUSH_TABLES` privilege(s).
- `binlog_rows_query_log_events=ON` enable original query event in binlog.

##### Kafka Config
`auto.create.topics.enable` should be true

### How to run
##### Start services
```shell
docker compose up -d
```

##### Create source connector
```shell
curl -i -X POST \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  localhost:8083/connectors/ \
  -d '{
    "name": "source-connector",
    "config": {
      "connector.class": "io.debezium.connector.mysql.MySqlConnector",
      "tasks.max": "1",
      "database.hostname": "mysql",
      "database.port": "3306",
      "database.user": "root",
      "database.password": "123456",
      "database.server.id": "184054",
      "topic.prefix": "mysql",
      "database.include.list": "coffee_shop",
      "schema.history.internal.kafka.bootstrap.servers": "kafka:9092",
      "schema.history.internal.kafka.topic": "schemahistory.coffee_shop"
    }
  }'
```

##### Start consumer
```shell
cd cdc-debezium-consumer
./gradlew run -d
```

### Troubleshooting

##### Check status of Kafka Connect service
```shell
curl -H "Accept:application/json" localhost:8083/
```

##### Check available plugins
```shell
curl -s localhost:8083/connector-plugins
```

##### Check list connectors registered with Kafka Connect
```shell
curl -H "Accept:application/json" localhost:8083/connectors/
```
#### Delete a connector
```shell
curl -X DELETE localhost:8083/connectors/source-connector
```

##### Access the kafka container
```shell
docker exec -it cdc-debezium-consumer-kafka-1 /bin/bash 
cd bin
```

##### List kafka topic
```shell
kafka-topics.sh --list --bootstrap-server localhost:9092
```
