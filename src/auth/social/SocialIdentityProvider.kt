package org.camuthig.auth.social

interface SocialIdentityProvider {
    suspend fun getIdentity(token: String): SocialIdentity
}