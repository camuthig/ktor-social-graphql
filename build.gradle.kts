import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    id("org.flywaydb.flyway") version "5.2.4"
    kotlin("jvm") version "1.3.20"
    id("org.camuthig.credentials") version "0.1.0"
}

group = "ktor-social-graphql"
version = "0.0.1"

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven {
        url = uri("https://dl.bintray.com/camuthig/maven")
    }
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    compile("io.ktor:ktor-server-netty:$ktor_version")
    compile("ch.qos.logback:logback-classic:$logback_version")
    compile("io.ktor:ktor-server-core:$ktor_version")
    compile("io.ktor:ktor-server-host-common:$ktor_version")
    compile("io.ktor:ktor-auth:$ktor_version")
    compile("io.ktor:ktor-auth-jwt:$ktor_version")
    compile("io.ktor:ktor-gson:$ktor_version")
    compile("io.ktor:ktor-client-core:$ktor_version")
    compile("io.ktor:ktor-client-core-jvm:$ktor_version")
    compile("io.ktor:ktor-client-apache:$ktor_version")
    implementation("io.ktor:ktor-locations:$ktor_version")
    implementation("io.ktor:ktor-html-builder:$ktor_version")
    implementation("org.postgresql:postgresql:42.2.5")
    implementation("org.camuthig.credentials:core:0.1.1")
    testCompile("io.ktor:ktor-server-tests:$ktor_version")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

// TODO use the defaults once the plugin supports it
credentials {
    credentialsFile = file("resources/credentials.conf.enc")
    masterKeyFile = file("resources/master.key")
}

flyway {
    locations = arrayOf("filesystem:resources/db/migration")
    user = credentials.getString("flyway.user")
    password = credentials.getString("flyway.password")
    url = credentials.getString("flyway.url")
}
