package org.camuthig.auth

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.OAuthServerSettings
import io.ktor.auth.oauth
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.http.HttpMethod
import io.ktor.locations.locations
import io.ktor.request.host
import org.camuthig.ktor.credentials

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

fun oauthProviders(application: Application) = listOf(
    OAuthServerSettings.OAuth2ServerSettings(
        name = "google",
        authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
        accessTokenUrl = "https://www.googleapis.com/oauth2/v3/token",
        requestMethod = HttpMethod.Post,

        clientId = application.credentials("auth.google.client.id"),
        clientSecret = application.credentials("auth.google.client.secret"),
        defaultScopes = listOf("https://www.googleapis.com/auth/plus.login")
    )
).associateBy { it.name }

private fun <T : Any> ApplicationCall.redirectUrl(t: T, secure: Boolean = true): String {
    val protocol = when {
        secure -> "https"
        else -> "http"
    }
    return "$protocol://${request.host()}${application.locations.href(t)}"
}