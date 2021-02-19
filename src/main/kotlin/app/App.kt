package app

import app.model.Division
import com.github.avrokotlin.avro4k.Avro
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.connect.json.JsonDeserializer
import org.apache.kafka.connect.json.JsonSerializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Predicate
import java.util.*

val appProperties = PropertiesLoader.loadProperties()
val inputTopic: String = appProperties.getProperty("topic.input")
val outputTopic: String = appProperties.getProperty("topic.output")
val errorTopic: String = appProperties.getProperty("topic.error")

fun main() {
    createTopics()

    val schema = Avro.default.schema(Division.serializer())

    val keySerde = Serdes.String()
    val valSerde = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())

    val builder = StreamsBuilder()
    val branches = builder.stream<String, Division>(inputTopic, Consumed.with(keySerde, valSerde))
        .peek { _, v -> println(v) }
        .mapValues(DivisionMapper())
        .branch(
            Predicate { _, value -> value.exception == null },
            Predicate { _, _ -> true }
        )
    branches[0]
        .mapValues(Unwrapper())
        .peek { _, v -> println(v) }
        .to(outputTopic)
    branches[1].to(errorTopic)
    val topology = builder.build()
    val streams = KafkaStreams(topology, streamsConfig())
    streams.start()
}

private fun streamsConfig(): Properties {
    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = appProperties.getProperty("application.id")
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = appProperties.getProperty("bootstrap.servers")
    props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
//    props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer()).javaClass
    return props
}

private fun createTopics() {
    val config = Properties()
    config[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = appProperties.getProperty("bootstrap.servers")
    val client = AdminClient.create(config)
    val topics = listOf(
        NewTopic(inputTopic, 1, 1),
        NewTopic(outputTopic, 1, 1),
        NewTopic(errorTopic, 1, 1)
    )
    client.createTopics(topics)
}