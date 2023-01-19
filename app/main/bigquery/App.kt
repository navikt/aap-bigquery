package bigquery

import bigquery.kafka.Topics
import bigquery.test.TableCreator
import com.google.cloud.bigquery.BigQueryOptions
import com.google.cloud.bigquery.Field
import com.google.cloud.bigquery.Schema
import com.google.cloud.bigquery.StandardSQLTypeName
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.aap.kafka.streams.KStreams
import no.nav.aap.kafka.streams.KafkaStreams
import no.nav.aap.kafka.streams.extension.consume
import no.nav.aap.ktor.config.loadConfig
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("Main")

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

fun Application.server(kafka: KStreams = KafkaStreams) {
    val config = loadConfig<Config>()
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) { registry = prometheus }

    kafka.connect(
        config = config.kafka,
        registry = prometheus,
        topology = topology()
    )

    routing {
        actuators(prometheus, kafka)
    }


    val tableCreator = TableCreator(BigQueryOptions.newBuilder().setProjectId("aap-dev-e48b").build().service)
    tableCreator.createTable("test1table", Schema.of(
        Field.newBuilder("name", StandardSQLTypeName.STRING)
            .setDescription("Navn").build(),
        Field.newBuilder("rank", StandardSQLTypeName.INT64)
            .setDescription("Rangering").build()
    ))
}

internal fun topology(): Topology {
    val streams = StreamsBuilder()

    streams
        .consume(Topics.vedtak)
        .peek {_, value -> log.info("Recieved ${value?.vedtaksid}")}

    return streams.build()
}