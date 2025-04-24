package com.mendoza

import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.slf4j.LoggerFactory
import kotlin.text.isBlank
import kotlin.text.lines

class FileTransferRoute : RouteBuilder() {

    private val log = LoggerFactory.getLogger(FileTransferRoute::class.java)

    private val expectedColumns = listOf(
        "id",
        "laboratorio_id",
        "paciente_id",
        "tipo_examen",
        "resultado",
        "fecha_examen"
    )

    fun validateCsvFile(exchange: Exchange) {
        val fileName = exchange.getIn()
            .getHeader(Exchange.FILE_NAME, String::class.java) ?: "unknown file"
        val bodyStr = exchange.getIn().getBody(String::class.java)

        if (bodyStr == null || bodyStr.isBlank()) {
            throw ValidationException("File '$fileName' is empty or could not be read.")
        }

        val lines = bodyStr.lines().filter { it.isNotBlank() }

        if (lines.isEmpty()) {
            throw ValidationException("File '$fileName' contains no data lines.")
        }

        val headerLine = lines.first()
        val actualHeaders = headerLine.split(",")
            .map { it.trim() }

        val missingColumns = expectedColumns.filter { !actualHeaders.contains(it) }
        if (missingColumns.isNotEmpty()) {
            throw ValidationException(
                "File '$fileName' is missing required columns: $missingColumns"
            )
        }

        val extraColumns = actualHeaders.filter { !expectedColumns.contains(it) }
        if (extraColumns.isNotEmpty()) {
            log.warn(
                "File '$fileName' contains extra columns not defined in expectedColumns: $extraColumns"
            )
        }

        log.debug("CSV content validation passed for '$fileName'")
    }

    override fun configure() {
        onException(ValidationException::class.java)
            .handled(true) // Keep handled(true) to prevent further propagation if needed
            .process { exchange ->
                val cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception::class.java)
                log.error(
                    exchange.getIn().getHeader(Exchange.FILE_NAME, String::class.java),
                )
                exchange.setException(cause) // Setting the exception again often works
            }
        onException(Exception::class.java)
            .handled(true)
            .process { exchange ->
                val cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception::class.java)
                log.error(
                    "Generic Error processing file [{}]: {}",
                    exchange.getIn().getHeader(Exchange.FILE_NAME, String::class.java),
                    cause?.message ?: "Unknown error",
                    cause // Log the full stack trace for generic errors
                )
                exchange.setException(cause)
            }

        from(
            "file:input-labs?move=processed/\${file:name}" +
                    "&moveFailed=error/\${file:name}" +
                    "&initialDelay=1000&delay=5000&autoCreate=true"
        )
            .routeId("csvFileTransferRoute")
            .log(LoggingLevel.INFO, "Processing file: \${header.CamelFileName}")
            .choice()
            .`when`(header(Exchange.FILE_NAME).endsWith(".csv"))
            .log(
                LoggingLevel.INFO,
                "File is CSV, proceeding with validation: \${header.CamelFileName}"
            )
            .log(LoggingLevel.INFO, ">>> Calling direct:insertIntoDb for \${header.CamelFileName}")
            .to("direct:insertIntoDb")
            .log(LoggingLevel.INFO, "<<< Returned from direct:insertIntoDb for \${header.CamelFileName}")

            .convertBodyTo(String::class.java)
            .process { exchange -> validateCsvFile(exchange) }
            .log(
                LoggingLevel.INFO,
                "File validated successfully: \${header.CamelFileName}"
            )
            .otherwise()
            .log(
                LoggingLevel.WARN,
                "File is not a CSV: \${header.CamelFileName}. Triggering error."
            )
            .throwException(ValidationException("File is not a CSV"))
            .endChoice()
    }

}