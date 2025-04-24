

plugins {
    kotlin("jvm") version "2.0.20"
}

group = "com.mendoza"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val camelVersion = "4.7.0"

dependencies {

    implementation("org.apache.camel:camel-core:$camelVersion")
    implementation("org.apache.camel:camel-main:$camelVersion")
    implementation("org.apache.camel:camel-kotlin-dsl:$camelVersion")
    implementation("org.apache.camel:camel-file:$camelVersion")

    // For CSV parsing
    implementation("org.apache.camel:camel-csv:$camelVersion") // Use your Camel version
    // For SQL interaction
    implementation("org.apache.camel:camel-sql:$camelVersion") // Use your Camel version
    // SQLite JDBC Driver
    implementation("org.xerial:sqlite-jdbc:3.49.1.0") // Check for the latest version
    // Optional: For DataSource configuration if not using a full framework
    implementation("org.apache.commons:commons-dbcp2:2.12.0") // Or HikariCP, etc.

    implementation("org.slf4j:slf4j-api:2.0.12")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.12") // Simple logger for console output


    testImplementation(kotlin("test"))

}



tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}



