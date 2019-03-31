package org.camuthig.auth

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.auth.oauth
import io.ktor.auth.session
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.locations.locations
import io.ktor.request.host
import io.ktor.routing.routing
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import org.camuthig.auth.config.JwtConfiguration
import org.camuthig.auth.config.OAuthConfiguration
import org.camuthig.auth.repository.RequeryUserRepository
import org.camuthig.ktor.database
import org.koin.dsl.module.module
import org.koin.ktor.ext.inject

data class Session(val accessToken: String)

val authModule = module {
    single<UserRepository> {
        RequeryUserRepository(database)
    }
}

fun Application.installAuth() {
    val userRepository: UserRepository by inject()
    val jwtRealm = environment.config.property("jwt.realm").getString()

    install(Sessions) {
        cookie<Session>("ktor_social_graphql_session") {
            cookie.path = "/"
        }
    }

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

        session<Session>("session") {
            validate {
                val session = sessions.get<Session>()

                if (session != null) {
                    val token = session.accessToken
                    val jwt = JwtConfiguration.verifier.verify(token)

                    val user = userRepository.getUser(jwt.subject.toInt())

                    if (user != null) {
                        UserPrincipal(user)
                    } else {
                        null
                    }
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
