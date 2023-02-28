package bigquery.tables.vedtak.v1

import bigquery.tables.TableCreator
import bigquery.tables.TableInserter
import com.google.cloud.bigquery.Field
import com.google.cloud.bigquery.Schema
import com.google.cloud.bigquery.StandardSQLTypeName
import no.nav.aap.dto.kafka.IverksettVedtakKafkaDto

interface BigQueryTable

class VedtakTable(
    tableCreator: TableCreator,
    private val tableInserter: TableInserter
): BigQueryTable {

    private val datasetName = "code_test_ds"
    private val tableName = "vedtak_v1_test"

    init {
        tableCreator.createTable(datasetName, tableName, Schema.of(
            Field.newBuilder("vedtaksid", StandardSQLTypeName.STRING)
                .setDescription("Intern vedtaksid i Kelvin").build(),
            Field.newBuilder("innvilget", StandardSQLTypeName.BOOL)
                .setDescription("Om vedtak er innvilget eller ikke").build(),
            Field.newBuilder("grunnlagsfaktor", StandardSQLTypeName.NUMERIC)
                .setDescription("Tall som representerer antall G").build(),
            Field.newBuilder("vedtaksdato", StandardSQLTypeName.TIMESTAMP)
                .setDescription("Dato vedtak ble fattet").build(),
            Field.newBuilder("virkningsdato", StandardSQLTypeName.TIMESTAMP)
                .setDescription("Dato vedtak skal virke fra").build(),
            Field.newBuilder("fodselsdato", StandardSQLTypeName.TIMESTAMP)
                .setDescription("Fødselsdato").build(),
            Field.newBuilder("fodselsnummer", StandardSQLTypeName.STRING)
                .setDescription("Fødselsdato").build()

        ))
    }

    fun insertVedtak(fnr: String, vedtak: IverksettVedtakKafkaDto) {
        tableInserter.insert(datasetName, tableName, vedtak.toInsertData(fnr))
    }

    private fun IverksettVedtakKafkaDto.toInsertData(fnr: String): Map<String, Any> {
        val data: MutableMap<String, Any> = HashMap()
        data["vedtaksid"] = this.vedtaksid.toString()
        data["innvilget"] = this.innvilget
        data["grunnlagsfaktor"] = this.grunnlagsfaktor
        data["vedtaksdato"] = this.vedtaksdato
        data["virkningsdato"] = this.virkningsdato
        data["fodselsdato"] = this.fødselsdato
        data["fodselsnummer"] = fnr
        return data
    }
}
