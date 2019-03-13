package org.camuthig.auth

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.auth.oauth
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.locations.locations
import io.ktor.request.host
import io.ktor.routing.routing
import org.camuthig.auth.config.JwtConfiguration
import org.camuthig.auth.config.OAuthConfiguration
import org.camuthig.ktor.database

fun Application.installAuth() {
    val userRepository = RequeryUserRepository(database)
    val jwtRealm = environment.config.property("jwt.realm").getString()

    install(Authentication) {
        oauth("oauth") {
            client = HttpClient(Apache)
            providerLookup = {
                OAuthConfiguration.oauthProviders[application.locations.resolve<LoginCallback>(LoginCallback::class, this).provider]?.first
            }
            urlProvider = { p -> redirectUrl(LoginCallback(p.name), false) }
        }

        jwt {
            realm = jwtRealm
            verifier(JwtConfiguration.verifier)
            validate { credential ->
                val user = userRepository.getUser(credential.payload.subject.toInt())

                if (user != null) {
                    UserPrincipal(user)
                } else {
                    null
                }
            }
        }
    }

    routing {
        authRoutes(userRepository)
    }
}

private fun <T : Any> ApplicationCall.redirectUrl(t: T, secure: Boolean = true): String {
    val protocol = when {
        secure -> "https"
        else -> "http"
    }
    return "$protocol://${request.host()}${application.locations.href(t)}"
}
