package app

import app.model.Division
import app.model.DivisionResult
import app.model.FailedResult
import app.util.DivisionMapper
import app.util.PropertiesLoader
import com.github.avrokotlin.avro4k.Avro
import com.github.thake.kafka.avro4k.serializer.Avro4kSerde
import io.confluent.kafka.schemaregistry.avro.AvroSchema
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Predicate
import org.apache.kafka.streams.kstream.Produced
import java.util.*

val appProperties = PropertiesLoader.loadProperties()
val inputTopic: String = appProperties.getProperty("topic.input")
val outputTopic: String = appProperties.getProperty("topic.output")
val errorTopic: String = appProperties.getProperty("topic.error")

fun main() {
    createTopics()
    val registryClient = CachedSchemaRegistryClient(appProperties.getProperty("schema.registry.url"), 10)
    registerSchemas(registryClient)
    val streams = KafkaStreams(buildTopology(registryClient), streamsConfig())
    streams.start()
}

private fun createTopics() {
    val config = Properties()
    config[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = appProperties.getProperty("bootstrap.servers")
    for (x in 1..60) {
        try {
            val client = AdminClient.create(config)
            val topics = listOf(
                NewTopic(inputTopic, 1, 1),
                NewTopic(outputTopic, 1, 1),
                NewTopic(errorTopic, 1, 1)
            )
            client.createTopics(topics)
            break
        } catch (e: Exception) {
            Thread.sleep(1000)
        }
    }
}

private fun registerSchemas(registryClient: CachedSchemaRegistryClient) {
    for (x in 1..60) {
        try {
            registryClient.register(
                "${inputTopic}-value",
                AvroSchema(Avro.default.schema(Division.serializer()))
            )
            registryClient.register(
                "${outputTopic}-value",
                AvroSchema(Avro.default.schema(DivisionResult.serializer()))
            )
            registryClient.register(
                "${errorTopic}-value",
                AvroSchema(Avro.default.schema(FailedResult.serializer()))
            )
            break
        } catch (e: Exception) {
            Thread.sleep(1000)
        }
    }
}

private fun buildTopology(registryClient: CachedSchemaRegistryClient): Topology {
    val keySerde = Serdes.String()
    val inputSerde = Avro4kSerde<Division>(registryClient)
    val outputSerde = Avro4kSerde<DivisionResult>(registryClient)
    val exceptionSerde = Avro4kSerde<FailedResult>(registryClient)

    val builder = StreamsBuilder()
    val branches = builder.stream(inputTopic, Consumed.with(keySerde, inputSerde))
        .mapValues(DivisionMapper())
        .branch(
            Predicate { _, result -> result is DivisionResult },
            Predicate { _, result -> result is FailedResult }
        )
    branches[0]
        .mapValues { result -> result as DivisionResult }
        .to(outputTopic, Produced.with(keySerde, outputSerde))
    branches[1]
        .mapValues { result -> result as FailedResult }
        .to(errorTopic, Produced.with(keySerde, exceptionSerde))
    return builder.build()
}

private fun streamsConfig(): Properties {
    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = appProperties.getProperty("application.id")
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = appProperties.getProperty("bootstrap.servers")
    props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
    return props
}
