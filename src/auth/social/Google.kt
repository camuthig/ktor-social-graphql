package org.camuthig.auth.social

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.client.request.header

class GoogleIdentityProvider: SocialIdentityProvider {
    data class GoogleIdentity(val sub: String, val name: String, val givenName: String, val email: String, val picture: String)

    override suspend fun getIdentity(token: String): SocialIdentity {
        val json = HttpClient(Apache).get<String>("https://www.googleapis.com/oauth2/v3/userinfo") {
            header("Authorization", "Bearer $token")
        }

        val googleIdentity = Gson()
            .newBuilder()
            .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
            .fromJson(json, GoogleIdentity::class.java)

        return SocialIdentity(googleIdentity.sub, googleIdentity.name, googleIdentity.givenName, googleIdentity.email, googleIdentity.picture)
    }
}