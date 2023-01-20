package bigquery.tables

import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.BigQueryException
import com.google.cloud.bigquery.TableId
import com.google.cloud.bigquery.TableInfo
import com.google.cloud.bigquery.ViewDefinition
import org.slf4j.LoggerFactory

class ViewCreator(
    private val bigQuery: BigQuery
) {

    private val log = LoggerFactory.getLogger("ViewCreator")

    fun creatView(dataset: String, viewName: String, query: String) {
        try {
            val tableId = TableId.of(dataset, viewName)
            val viewDefinition = ViewDefinition.newBuilder(query).setUseLegacySql(false).build()
            bigQuery.create(TableInfo.of(tableId, viewDefinition))
            log.info("View laget")
        } catch(e: BigQueryException) {
            log.error("Klarte ikke lage view", e)
            throw e
        }
    }

    fun updateView(dataset: String, viewName: String, query: String) {
        try {
            val viewMetadata = bigQuery.getTable(TableId.of(dataset, viewName))
            val viewDefinition = viewMetadata.getDefinition<ViewDefinition>()
            viewDefinition.toBuilder().setQuery(query).build()
            bigQuery.update(viewMetadata.toBuilder().setDefinition(viewDefinition).build())
        } catch(e: BigQueryException) {
            log.error("Klarte ikke oppdatere view", e)
            throw e
        }
    }

}