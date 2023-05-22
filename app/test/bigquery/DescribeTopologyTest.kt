package bigquery

import bigquery.tables.vedtak.v1.BigQueryTable
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.aap.kafka.streams.v2.config.StreamsConfig
import no.nav.aap.kafka.streams.v2.test.StreamsMock
import org.junit.jupiter.api.Test
import java.io.File

class DescribeTopologyTest {

    @Test
    fun mermaid() {
        val topology = topology(vedtakstable = object:BigQueryTable{})

        val kafka = StreamsMock()
        kafka.connect(topology, StreamsConfig("", ""), SimpleMeterRegistry())

        val mermaid = kafka.visulize().mermaid().generateDiagram()
        File("../docs/topology.mmd").apply { writeText(mermaid) }
    }
}