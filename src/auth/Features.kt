package org.camuthig.auth

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.oauth
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.locations.locations
import io.ktor.request.host

fun Application.installAuth() {
    install(Authentication) {
        oauth {
            client = HttpClient(Apache)
            providerLookup = {
                oauthProviders(application)[application.locations.resolve<LoginCallback>(LoginCallback::class, this).provider]
            }
            urlProvider = { p -> redirectUrl(LoginCallback(p.name), false) }
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
