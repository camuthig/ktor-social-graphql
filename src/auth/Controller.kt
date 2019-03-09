package org.camuthig.auth

import io.ktor.application.call
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.locations.Location
import io.ktor.locations.location
import io.ktor.routing.Routing
import io.ktor.routing.param

@Location("/login/{provider?}")
data class Login(val provider: String = "")

fun Routing.authRoutes() {
    authenticate {
        location<Login> {
            param("error") {
                handle {
                    call.loginFailedPage(call.parameters.getAll("error").orEmpty())
                }
            }

            handle {
                val principal = call.authentication.principal<OAuthAccessTokenResponse>()
                if (principal != null) {
                    call.loggedInSuccessResponse(principal)
                } else {
                    call.loginPage(oauthProviders(call.application))
                }
            }
        }
    }
}