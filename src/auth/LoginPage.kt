package org.camuthig.auth

import io.ktor.application.ApplicationCall
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.OAuthServerSettings
import io.ktor.html.Template
import io.ktor.locations.locations
import kotlinx.html.*

class LoginPage(private val call: ApplicationCall) : Template<HTML> {
    override fun HTML.apply() {
        head {
            title { +"Login with" }
        }
        body {
            h1 {
                +"Login with:"
            }

            for (p in OAuthConfiguration.oauthProviders) {
                p {
                    a(href = call.application.locations.href(LoginCallback(p.key))) {
                        +p.key
                    }
                }
            }
        }
    }
}
