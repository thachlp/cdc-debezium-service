##### Create JDBC Connector 
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
