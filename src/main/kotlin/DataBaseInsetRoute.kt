package com.mendoza

import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel
// Import PredicateBuilder
import org.apache.camel.builder.PredicateBuilder
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.dataformat.CsvDataFormat
import org.slf4j.LoggerFactory
import java.sql.SQLException

class DatabaseInsertRoute : RouteBuilder() {

    private val log = LoggerFactory.getLogger(DatabaseInsertRoute::class.java)

    private val insertSql = """
        INSERT OR IGNORE INTO results (id, laboratorio_id, paciente_id, tipo_examen, resultado, fecha_examen)
        VALUES (:#id, :#laboratorio_id, :#paciente_id, :#tipo_examen, :#resultado, :#fecha_examen)
    """.trimIndent()

    override fun configure() {
        val csvConfiguredFormat = CsvDataFormat().apply {
            setUseMaps("true")
            setTrim("true")
            setSkipHeaderRecord("true")
        }

        // --- Exception Handling ---
        onException(SQLException::class.java)
            .handled(true)
            .log(
                LoggingLevel.ERROR,
                log,
                "SQL Error during insert for \${header.CamelFileName}. Row data: \${body}. Error: \${exception.message} \nStack Trace: \${exception.stacktrace}"
            )

        onException(Exception::class.java)
            .handled(true)
            .log(
                LoggingLevel.ERROR,
                log,
                "!!! Unexpected Error in DatabaseInsertRoute for \${header.CamelFileName}. Current Body: \${body}. Error: \${exception.message} \nStack Trace: \${exception.stacktrace}"
            )

        // --- Route Logic ---
        from("direct:insertIntoDb")
            .routeId("databaseInsertRoute")
            .log(LoggingLevel.INFO, log, "Received data for DB insertion from \${header.CamelFileName}")
            // *** ADD THIS LOG ***
            .log(LoggingLevel.DEBUG, log, "Body received by DB route (before unmarshal): \n\${body}")
            .unmarshal(csvConfiguredFormat)
            .log(
                LoggingLevel.DEBUG,
                log,
                "Unmarshalled CSV Body Type: \${body.getClass().getName()}"
            )
            .log(
                LoggingLevel.TRACE,
                log,
                "Unmarshalled CSV Body Content: \${body}"
            )

            // Step 1.5: Check if the list is empty after unmarshalling
            .choice()
            .`when`(PredicateBuilder.or(
                body().isNull(),
                simple("\${body.size} == 0")
            ))
            .log(
                LoggingLevel.WARN,
                log,
                "CSV file \${header.CamelFileName} resulted in empty data list after unmarshalling (or was empty)."
            )
            .stop() // Stop processing this exchange if no data rows
            .otherwise()
            .log(
                LoggingLevel.DEBUG,
                log,
                "CSV unmarshalled successfully for \${header.CamelFileName}, proceeding to split."
            )
            .endChoice() // End the choice block itself
            // *** ADD .end() HERE to return to the main route flow ***
            .end()

            // Step 2: Split into Rows (Now correctly placed after the choice block)
            .split(body())
            .streaming()
            .log(
                LoggingLevel.DEBUG,
                log,
                "Splitter processing row map: \${body}"
            )
            .log(
                LoggingLevel.DEBUG,
                log,
                "Attempting SQL insert for row: \${body}"
            )
            .toD(
                "sql:$insertSql?dataSource=#sqliteDataSource"
            )
            .log(
                LoggingLevel.DEBUG,
                log,
                "SQL insert attempt completed for row: \${body}"
            )
            .end() // End of splitter block
            .log(
                LoggingLevel.INFO,
                log,
                "Finished splitting and processing all rows for \${header.CamelFileName}"
            )
            .log(
                LoggingLevel.INFO,
                log,
                "Finished database insertion route successfully for \${header.CamelFileName}"
            )
    }
}
