package org.camuthig

import io.ktor.application.*
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.locations.Locations
import org.camuthig.auth.*
import org.camuthig.ktor.database

fun main(args: Array<String>): Unit {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val userRepository = RequeryRepository(database)
    install(Locations)
    installAuth(userRepository)

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        authRoutes(userRepository)

        authenticate {
            get("/me") {
                val principal: UserPrincipal = call.authentication.principal()!!

                call.respond(principal.user)
            }
        }
    }
}
