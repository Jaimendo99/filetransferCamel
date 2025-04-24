package com.mendoza

import org.apache.camel.main.Main
import org.slf4j.LoggerFactory
import org.sqlite.SQLiteDataSource

class ValidationException(message: String) : Exception(message)

fun main() {
    val log = LoggerFactory.getLogger("com.mendoza.MainKt") // Logger for main
    val main = Main()

    // --- Database Setup ---
    val dbFile = "bionet_data.db"
    val dataSource = SQLiteDataSource()
    dataSource.url = "jdbc:sqlite:$dbFile"
    log.info("Using database file: {}", dbFile) // Use logger

    main.bind("sqliteDataSource", dataSource)

    try {
        DatabaseInitializer.createTables(dataSource)
    } catch (e: RuntimeException) {
        log.error("ERROR: Could not initialize database schema. Exiting.", e) // Use logger
        return
    }

    // --- Camel Setup ---
    log.info("Configuring Camel routes...") // Use logger
    // Add routes using a cleaner approach
    main.configure().apply {
        addRoutesBuilder(FileTransferRoute())
        addRoutesBuilder(DatabaseInsertRoute())
    }

    try {
        log.info("Starting Camel context and waiting...")
        main.start()
        main.run() //
        log.info("Camel application finished.") // Use logger

    } catch (e: Exception) {
        log.error("An error occurred during Camel execution:", e) // Use logger
    } finally {
        if (main.isStarted && !main.isStopped) {
            log.info("Stopping Camel context.")
            main.stop()
        }
    }
}