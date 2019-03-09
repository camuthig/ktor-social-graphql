package org.camuthig.auth

import io.ktor.application.call
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.html.respondHtmlTemplate
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.location
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.param

@Location("/login/{provider}/callback")
data class LoginCallback(val provider: String)

fun Routing.authRoutes() {
    get("/login") {
        call.respondHtmlTemplate(LoginPage(call, oauthProviders(call.application)), HttpStatusCode.OK) {}
    }

    authenticate {
        location<LoginCallback> {
            param("error") {
                handle {
                    call.respondHtmlTemplate(LoginFailure(call.parameters.getAll("error").orEmpty()), HttpStatusCode.OK) {}
                }
            }

            handle {
                val principal = call.authentication.principal<OAuthAccessTokenResponse>()
                if (principal != null) {
                    call.respondHtmlTemplate(LoginSuccess(principal), HttpStatusCode.OK) {}
                } else {
                    call.respondHtmlTemplate(LoginFailure(listOf("Unable to find principal")), HttpStatusCode.OK) {}
                }
            }
        }
    }
}