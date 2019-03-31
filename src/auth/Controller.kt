package org.camuthig.auth

import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.location
import io.ktor.locations.locations
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.param
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import org.camuthig.auth.config.JwtConfiguration
import org.camuthig.auth.config.OAuthConfiguration
import org.camuthig.auth.view.LoginPage
import org.camuthig.ktor.respondView

@Location("/login/{provider}/callback")
data class LoginCallback(val provider: String)

fun Routing.authRoutes(userRepository: UserRepository) {
    get("/login") {
        call.respondView(LoginPage(call))
    }

    authenticate {
        get("/me") {
            val principal: UserPrincipal = call.authentication.principal()!!

            call.respond(principal.user)
        }
    }

    authenticate("oauth") {
        location<LoginCallback> {
            param("error") {
                handle {
                    call.respond(HttpStatusCode.BadRequest, call.parameters.getAll("error").orEmpty())
                }
            }

            handle {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()

                if (principal != null) {
                    val providerName = call.application.locations.resolve<LoginCallback>(LoginCallback::class, call).provider
                    val oauthConfiguration = OAuthConfiguration.oauthProviders[providerName]
                    if (oauthConfiguration != null) {
                        val socialIdentity = oauthConfiguration.second.getIdentity(principal.accessToken)

                        // TODO should handle errors from this call as well
                        var user = userRepository.getUser(providerName, socialIdentity)

                        if (user == null) {
                            user = userRepository.linkIdentity(providerName, socialIdentity)
                        }

                        call.sessions.set(Session(JwtConfiguration.makeToken(user)))

                        call.respondRedirect("/graphql-playground")
                    } else {
                        call.application.log.error("Missing identity provider configuration for $providerName")
                        call.respond(HttpStatusCode.NotFound)
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }
    }
}