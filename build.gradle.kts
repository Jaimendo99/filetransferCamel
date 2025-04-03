

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



