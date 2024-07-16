### CDC Consumer for MySQL
### Prerequisite

##### Install [Docker](script/install_docker.sh), [Docker Compose](script/install_docker_compose.sh)

##### MySQL Config
- `log_bin` should be ON
- `binlog_format` should be ROW
- `binlog_row_image` should be FULL
- Verify:
    ```shell
    SHOW VARIABLES LIKE 'log_bin'; -- ON
    SHOW VARIABLES LIKE 'binlog_format'; -- ROW
    SHOW VARIABLES LIKE 'binlog_row_image'; -- FULL
    ```

##### Kafka Config
`auto.create.topics.enable` should be true

### How to run
##### Start services
```shell
docker compose up -d
```

##### Check status of Kafka Connect service
```shell
curl -H "Accept:application/json" localhost:8083/
```

##### Check list connectors registered with Kafka Connect
```shell
curl -H "Accept:application/json" localhost:8083/connectors/
curl -s localhost:8083/connector-plugins

```
#### Delete a connector
```shell
curl -X DELETE localhost:8083/connectors/sink-connector
curl -X DELETE localhost:8083/connectors/source-connector
```

##### Start source connector
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
```shell
curl -i -X POST \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  localhost:8083/connectors/ \
  -d '{
    "name": "target-connector",  
    "config": {
        "connector.class": "io.debezium.connector.jdbc.JdbcSinkConnector",  
        "tasks.max": "1",  
        "connection.url": "jdbc:mysql://cdc-debezium-consumer-mysql-1:3306/coffee_shop_v2",  
        "connection.username": "root",  
        "connection.password": "123456",  
        "insert.mode": "upsert",  
        "delete.enabled": "true",  
        "primary.key.mode": "record_key",  
        "schema.evolution": "basic",  
        "database.time_zone": "UTC",  
        "topics": "mysql.coffee_shop.category" 
        }
    }'

```



##### Start consumer
```shell
./gradlew run -d
```

### Troubleshooting

##### Access the kafka container
```shell
docker exec -it cdc-debezium-consumer-kafka-1 /bin/bash 
cd bin
```

##### List kafka topic
```shell
kafka-topics.sh --list --bootstrap-server localhost:9092
```
