package org.camuthig.auth.config

import io.ktor.auth.OAuthServerSettings
import io.ktor.http.HttpMethod
import org.camuthig.auth.social.GoogleIdentityProvider
import org.camuthig.auth.social.SocialIdentityProvider
import org.camuthig.ktor.Credentials

object OAuthConfiguration {
    val oauthProviders = listOf(
        Pair<OAuthServerSettings, SocialIdentityProvider>(
            OAuthServerSettings.OAuth2ServerSettings(
                name = "google",
                authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                accessTokenUrl = "https://www.googleapis.com/oauth2/v3/token",
                requestMethod = HttpMethod.Post,

                clientId = Credentials.get("auth.google.client.id"),
                clientSecret = Credentials.get("auth.google.client.secret"),
                defaultScopes = listOf("openid", "profile", "email")
            ),
            GoogleIdentityProvider()
        )
    ).associateBy { it.first.name }
}
