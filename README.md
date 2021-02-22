1. Run `docker build -t kafka-kotlin-example .`
2. Run `docker-compose up -d`
3. Start avro console consumer for output topic in schema-registry container `kafka-avro-console-consumer --topic output-topic --bootstrap-server kafka:9092`
4. Start avro console consumer for error topic in schema-registry container `kafka-avro-console-consumer --topic error-topic --bootstrap-server kafka:9092`
5. Start avro console producer in schema-registry container
```
kafka-avro-console-producer --topic input-topic --bootstrap-server kafka:9092 --property value.schema.id=`curl -X GET http://localhost:8081/schemas | grep -Po 'input-topic-value","version":\d,"id":\K(\d+)'`
```
6. Try differend inputs in format `{"dividend": 121, "divisor": 11}` in producer
