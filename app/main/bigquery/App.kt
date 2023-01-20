package bigquery

import bigquery.kafka.Topics
import bigquery.tables.TableCreator
import bigquery.tables.TableInserter
import bigquery.tables.vedtak.v1.VedtakTable
import com.google.cloud.bigquery.BigQueryOptions
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
import no.nav.aap.kafka.streams.extension.filterNotNull
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

    val bigQuery = BigQueryOptions.newBuilder().setProjectId(config.bigquery.project).build().service
    val tableCreator = TableCreator(bigQuery)
    val tableInserter = TableInserter(bigQuery)

    val vedtakTable = VedtakTable(tableCreator, tableInserter)

    kafka.connect(
        config = config.kafka,
        registry = prometheus,
        topology = topology(vedtakTable)
    )

    routing {
        actuators(prometheus, kafka)
    }
}

internal fun topology(vedtakstable: VedtakTable): Topology {
    val streams = StreamsBuilder()

    streams
        .consume(Topics.vedtak)
        .filterNotNull("skip-vedtak-tombstone")
        .peek { _, value -> log.info("Recieved ${value?.vedtaksid}") }
    // TODO: Insert objekt

    return streams.build()
}
