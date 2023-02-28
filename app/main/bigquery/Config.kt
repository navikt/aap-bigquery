package bigquery

import no.nav.aap.kafka.streams.v2.config.StreamsConfig


internal data class Config(
    val bigquery: BigQueryConfig,
    val kafka: StreamsConfig,
)

internal data class BigQueryConfig(
    val project: String
)