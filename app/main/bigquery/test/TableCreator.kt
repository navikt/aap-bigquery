package bigquery.test

import com.google.cloud.bigquery.*
import org.slf4j.LoggerFactory

class TableCreator(
    val bigQuery: BigQuery
) {
    val log = LoggerFactory.getLogger("TableCreator")
    val dataset = "code_test_ds"

    fun createTable(tableName: String, schema: Schema) {

        val table = bigQuery.getTable(TableId.of(dataset, tableName))
        if (table != null && table.exists()) {
            log.info("Table $tableName eksisterer allerede")
            return
        }

        try {
            // Initialize client that will be used to send requests. This client only needs to be created
            // once, and can be reused for multiple requests.
            val tableId = TableId.of(dataset, tableName)
            val tableDefinition: TableDefinition = StandardTableDefinition.of(schema)
            val tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build()
            val create = bigQuery.create(tableInfo)
            log.info("Table $tableName created successfully: ${create.tableId.iamResourceName}")
        } catch (e: BigQueryException) {
            log.error("Table was not created.,", e)
            throw e
        }
    }

    fun addColumn(tableName: String, colname: String) {
        try {
            val table = requireNotNull(bigQuery.getTable(TableId.of(dataset, tableName))) { "Tabell finnes ikke" }
            val def: StandardTableDefinition = table.getDefinition()
            val schema = requireNotNull(def.schema) { "Schema ikke funnet" }
            val fields = schema.fields
            val newField = Field.newBuilder(colname, StandardSQLTypeName.STRING)
                .setDescription("Hmm").setMode(Field.Mode.NULLABLE)
                .build()
            val fieldlist = mutableListOf<Field>()
            fields.forEach(fieldlist::add)
            fieldlist.add(newField)
            val newSchema = Schema.of(fieldlist)
            val updatedTable = table.toBuilder().setDefinition(StandardTableDefinition.of(newSchema)).build()
            updatedTable.update()
            log.info("Table updated")
        } catch (e : BigQueryException) {
            log.error("Table was not updated.", e)
            throw e
        }
    }

}