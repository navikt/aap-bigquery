package bigquery.tables

import com.google.cloud.bigquery.*
import org.slf4j.LoggerFactory

class TableCreator(
    private val bigQuery: BigQuery
) {
    private val log = LoggerFactory.getLogger("TableCreator")

    fun createTable(dataset: String, tableName: String, schema: Schema) {

        val table = bigQuery.getTable(TableId.of(dataset, tableName))
        if (table != null && table.exists()) {
            log.debug("Table $tableName eksisterer allerede")
            return
        }

        try {
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

    fun addColumn(
        dataset: String,
        tableName: String,
        colname: String,
        type: StandardSQLTypeName,
        description: String
    ) {
        try {
            val table = requireNotNull(bigQuery.getTable(TableId.of(dataset, tableName))) { "Tabell finnes ikke" }
            val def: StandardTableDefinition = table.getDefinition()
            val schema = requireNotNull(def.schema) { "Schema ikke funnet for $tableName" }
            val fields = schema.fields
            if(fields.get(colname) == null) {
                log.warn("Kolonne $colname finnes allerede i $tableName")
            }
            val newField = Field.newBuilder(colname, type)
                .setDescription(description)
                .setMode(Field.Mode.NULLABLE) // Må være nullable for å få lov til å lage
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

    fun deleteTable(dataset: String, tableName: String) {
        try {
            val success = bigQuery.delete(TableId.of(dataset, tableName))
            if (success) {
                log.info("Tabellen $tableName slettet.")
            } else {
                log.info("Tabellen $tableName ikke funnet for sletting.")
            }
        } catch (e : BigQueryException) {
            log.error("Table was not updated.", e)
            throw e
        }
    }
}
