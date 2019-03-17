package org.camuthig

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.locations.Locations
import org.camuthig.auth.*
import org.camuthig.graphql.installGraphQL
import org.koin.standalone.StandAloneContext.startKoin

fun main(args: Array<String>): Unit {
    startKoin(listOf(authModule))
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Locations)
    installAuth()
    installGraphQL()

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }
    }
}
