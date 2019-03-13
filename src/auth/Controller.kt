package org.camuthig.auth

import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.html.respondHtmlTemplate
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.location
import io.ktor.locations.locations
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.param
import org.camuthig.ktor.JwtConfig
import org.camuthig.ktor.respondView

@Location("/login/{provider}/callback")
data class LoginCallback(val provider: String)

data class LoginResponse(val accessToken: String)

fun Routing.authRoutes(repository: Repository) {
    get("/login") {
        call.respondView(LoginPage(call, oauthProviders(call.application)))
    }

    authenticate("oauth") {
        location<LoginCallback> {
            param("error") {
                handle {
                    call.respondHtmlTemplate(LoginFailure(call.parameters.getAll("error").orEmpty()), HttpStatusCode.OK) {}
                }
            }

            handle {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()

                if (principal != null) {
                    val providerName = call.application.locations.resolve<LoginCallback>(LoginCallback::class, call).provider
                    val identityProvider = identityProviders[providerName]
                    if (identityProvider != null) {
                        val socialIdentity = identityProvider.getIdentity(principal.accessToken)

                        // TODO should handle errors from this call as well
                        var user = repository.getUser(providerName, socialIdentity)

                        if (user == null) {
                            user = repository.linkIdentity(providerName, socialIdentity)
                        }

                        call.respond(LoginResponse(JwtConfig.makeToken(user)))
                    } else {
                        call.application.log.error("Unable to find identity provider configuration for $providerName")
                        call.respondView(LoginFailure(listOf("Unable to get user information from the login provider")))
                    }
                } else {
                    call.respondView(LoginFailure(listOf("Unable to find principal")))
                }
            }
        }
    }
}