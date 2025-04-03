package com.mendoza

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.main.Main

class FileReadingRoute : RouteBuilder() {
    override fun configure() {
        from("file:files/input?noop=true")
            .log("Copying file: \${header.CamelFileName} to output directory")
            .to("file:files/output")
    }
}

fun main() {
    val main = Main()
    main.configure().addRoutesBuilder(FileReadingRoute())
    main.run()
}