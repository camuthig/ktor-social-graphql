package org.camuthig.auth.view

import io.ktor.application.ApplicationCall
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.OAuthServerSettings
import io.ktor.html.Template
import io.ktor.locations.locations
import kotlinx.html.*
import org.camuthig.auth.LoginCallback
import org.camuthig.auth.config.OAuthConfiguration

class LoginPage(private val call: ApplicationCall) : Template<HTML> {
    override fun HTML.apply() {
        head {
            title { +"Login with" }
        }
        body {
            h1 {
                +"Login with:"
            }

            a {
                href = call.application.locations.href(LoginCallback("google"))
                button {
                    +"Google"
                }
            }
        }
    }
}
