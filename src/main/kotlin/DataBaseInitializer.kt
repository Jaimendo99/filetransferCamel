package com.mendoza

import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

object DatabaseInitializer {
    private val createTableSql = """
        CREATE TABLE IF NOT EXISTS laboratories (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            city TEXT NOT NULL
        );

        CREATE TABLE IF NOT EXISTS client (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL
        );

        CREATE TABLE IF NOT EXISTS exam_type (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            typename TEXT NOT NULL UNIQUE
        );

        CREATE TABLE IF NOT EXISTS results (
            id INTEGER PRIMARY KEY,
            laboratorio_id TEXT NOT NULL,
            paciente_id TEXT NOT NULL,
            tipo_examen TEXT NOT NULL,
            resultado TEXT,
            fecha_examen TEXT
        );

        CREATE INDEX IF NOT EXISTS idx_results_lab ON results (laboratorio_id);
        CREATE INDEX IF NOT EXISTS idx_results_patient ON results (paciente_id);
        CREATE INDEX IF NOT EXISTS idx_results_date ON results (fecha_examen);
    """.trimIndent() // Use trimIndent for cleaner multiline strings

    val log = LoggerFactory.getLogger("com.mendoza.MainKt") // Logger for main

    fun createTables(dataSource: DataSource) {
        log.info("Initializing database schema...") // Log the initialization
        val sqlStatements = createTableSql.split(';')
            .map { it.trim() } // Remove leading/trailing whitespace
            .filter { it.isNotEmpty() } // Remove empty statements

        try {
            dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    for (sql in sqlStatements) {
                        try {
                            log.info("Executing SQL statement: $sql") // Log the SQL statement
                            statement.execute(sql)
                        } catch (e: SQLException) {
                            log.error("SQL Error executing statement: $sql, Error: ${e.message}")
                            // Handle specific SQL errors if needed
                            // For example, you can check for specific error codes or messages
                            // and take appropriate action (e.g., retry, skip, etc.)
                        } catch (e: Exception) {
                            log.error("Unexpected error executing statement: $sql, Error: ${e.message}")
                            // Handle unexpected errors
                            // You may want to rethrow or log them as needed
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            // Throw a runtime exception to halt application startup if schema init fails critically
            log.error("SQL Error during schema initialization: ${e.message}")
            throw RuntimeException("Database schema initialization failed", e)
        } catch (e: Exception) {
            log.error("Unexpected error during schema initialization: ${e.message}")
            throw RuntimeException("Database schema initialization failed", e)
        }
    }
}
