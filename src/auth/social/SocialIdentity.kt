package org.camuthig.auth.social

data class SocialIdentity(val id: String, val name: String, val nickname: String, val email: String, val avatar: String)

interface SocialIdentityProvider {
    suspend fun getIdentity(token: String): SocialIdentity
}