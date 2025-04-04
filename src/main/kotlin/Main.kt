package com.mendoza

import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.main.Main


class FileReadingRoute : RouteBuilder() {

    fun getFileType(exchange: Exchange): FileType? {
        val fileName = exchange.getIn()
            ?.getHeader("CamelFileName", String::class.java)
        return when {
            fileName?.endsWith(".csv", ignoreCase = true) == true -> FileType.CSV
            else -> null
        }
    }

    fun processCsvFile(exchange: Exchange): Result<Boolean> {
        val bodyStr = exchange.getIn().getBody(String::class.java)
        val lines = bodyStr.lines().filter { it.isNotBlank() }
        if (lines.isNotEmpty()) {
            val headerLine = lines.first()
            headerLine.split(",").map { it.trim() }.forEach {
                if (!columns.contains(it))
                    return Result.failure(IllegalArgumentException("Invalid column name: $it"))
            }
        } else println("CSV file is empty")

        return Result.success(true)

    }

    override fun configure() {
        onException(IllegalArgumentException::class.java)
            .maximumRedeliveries(0)
            .handled(true)
            .log( LoggingLevel.ERROR,  "${'$'}{exception.message}" )
        from("file:files/input?noop=true").convertBodyTo(String::class.java)
            .process { exchange -> exchange.getIn().setHeader("fileType", getFileType(exchange)) }
            .process { exchange ->
                val fileType: FileType? = exchange.getIn().getHeader("fileType", FileType::class.java)
                when (fileType) {
                    FileType.CSV -> {
                        processCsvFile(exchange)
                            .onFailure { throw(IllegalArgumentException("Error processing CSV file: ${it.message}")) }
                    }

                    null -> println("File type not supported")
                }
            }
            .to("file:files/output?fileExist=Override").apply {
                log(LoggingLevel.INFO, "File processed successfully: ${'$'}{header.CamelFileName}")
            }
    }
}


fun main() {
    val main = Main()
    main.configure().addRoutesBuilder(FileReadingRoute())
    main.run()
}

val columns = listOf("date", "client", "product", "price", "quantity")


enum class FileType {
    CSV
}