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
import org.camuthig.ktor.JwtConfig

fun Application.installAuth(userRepository: Repository) {
    val jwtRealm = environment.config.property("jwt.realm").getString()

    install(Authentication) {
        oauth("oauth") {
            client = HttpClient(Apache)
            providerLookup = {
                oauthProviders(application)[application.locations.resolve<LoginCallback>(LoginCallback::class, this).provider]
            }
            urlProvider = { p -> redirectUrl(LoginCallback(p.name), false) }
        }

        jwt {
            realm = jwtRealm
            verifier(JwtConfig.verifier)
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
}

private fun <T : Any> ApplicationCall.redirectUrl(t: T, secure: Boolean = true): String {
    val protocol = when {
        secure -> "https"
        else -> "http"
    }
    return "$protocol://${request.host()}${application.locations.href(t)}"
}
