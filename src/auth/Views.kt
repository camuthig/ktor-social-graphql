package org.camuthig.auth

import io.ktor.application.ApplicationCall
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.OAuthServerSettings
import io.ktor.html.respondHtml
import io.ktor.locations.locations
import kotlinx.html.*

// TODO Clean up this code to be templates instead
suspend fun ApplicationCall.loginPage(loginProviders: Map<String, OAuthServerSettings>) {
    respondHtml {
        head {
            title { +"Login with" }
        }
        body {
            h1 {
                +"Login with:"
            }

            for (p in loginProviders) {
                p {
                    a(href = application.locations.href(Login(p.key))) {
                        +p.key
                    }
                }
            }
        }
    }
}

suspend fun ApplicationCall.loginFailedPage(errors: List<String>) {
    respondHtml {
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

suspend fun ApplicationCall.loggedInSuccessResponse(callback: OAuthAccessTokenResponse) {
    respondHtml {
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