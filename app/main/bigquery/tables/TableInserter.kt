package bigquery.tables

import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.TableId
import org.slf4j.LoggerFactory

class TableInserter(
    private val bigQuery: BigQuery
) {

    private val log = LoggerFactory.getLogger("TableInserter")

    fun insert(dataset: String, tableName: String, data: Map<String, Any>) {
        val insertAll = bigQuery.insertAll(
            InsertAllRequest.newBuilder(TableId.of(dataset, tableName))
                .also { builder ->
                    builder.addRow(data)
                }
                .build()
        )

        if (insertAll != null && insertAll.hasErrors()) {
            insertAll.insertErrors.forEach { (t, u) -> log.error("$t - $u") }
            throw RuntimeException("Bigquery insert har errors")
        }
    }

}
