package bigquery

import no.nav.aap.kafka.streams.KStreamsConfig

internal data class Config(
    val bigquery: BigQueryConfig,
    val kafka: KStreamsConfig,
)

internal data class BigQueryConfig(
    val project: String
)