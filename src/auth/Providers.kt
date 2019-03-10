package org.camuthig.auth

import io.ktor.application.Application
import io.ktor.auth.OAuthServerSettings
import io.ktor.http.HttpMethod
import org.camuthig.auth.social.GoogleIdentityProvider
import org.camuthig.auth.social.SocialIdentityProvider
import org.camuthig.ktor.credentials

fun oauthProviders(application: Application) = listOf(
    OAuthServerSettings.OAuth2ServerSettings(
        name = "google",
        authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
        accessTokenUrl = "https://www.googleapis.com/oauth2/v3/token",
        requestMethod = HttpMethod.Post,

        clientId = application.credentials("auth.google.client.id"),
        clientSecret = application.credentials("auth.google.client.secret"),
        defaultScopes = listOf("openid", "profile", "email")
    )
).associateBy { it.name }

val identityProviders = mapOf<String, SocialIdentityProvider>(
    "google" to GoogleIdentityProvider()
)
