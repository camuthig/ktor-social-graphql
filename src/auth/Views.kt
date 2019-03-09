package org.camuthig.auth

import io.ktor.application.ApplicationCall
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.OAuthServerSettings
import io.ktor.html.Template
import io.ktor.locations.locations
import kotlinx.html.*

// TODO Make the `call` parameter more implicit to all templates somehow. It should always be there.
class LoginPage(private val call: ApplicationCall, private val loginProviders: Map<String, OAuthServerSettings>) : Template<HTML> {
    override fun HTML.apply() {
        head {
            title { +"Login with" }
        }
        body {
            h1 {
                +"Login with:"
            }

            for (p in loginProviders) {
                p {
                    a(href = call.application.locations.href(LoginCallback(p.key))) {
                        +p.key
                    }
                }
            }
        }
    }
}

class LoginFailure(private val errors: List<String>): Template<HTML> {
    override fun HTML.apply() {
        head {
            title { +"Login with" }
        }
        body {
            h1 {
                +"Login error"
            }

            for (e in errors) {
                p {
                    +e
                }
            }
        }
    }
}

class LoginSuccess(private val callback: OAuthAccessTokenResponse): Template<HTML> {
    override fun HTML.apply() {
        head {
            title { +"Logged in" }
        }
        body {
            h1 {
                +"You are logged in"
            }
            p {
                +"Your token is $callback"
            }
        }
    }
}
