package org.camuthig.auth

import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.html.respondHtmlTemplate
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.location
import io.ktor.locations.locations
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.param
import io.requery.kotlin.eq
import org.camuthig.ktor.database
import org.camuthig.ktor.respondView

@Location("/login/{provider}/callback")
data class LoginCallback(val provider: String)

fun Routing.authRoutes() {
    get("/login") {
        call.respondView(LoginPage(call, oauthProviders(call.application)))
    }

    authenticate {
        location<LoginCallback> {
            param("error") {
                handle {
                    call.respondHtmlTemplate(LoginFailure(call.parameters.getAll("error").orEmpty()), HttpStatusCode.OK) {}
                }
            }

            handle {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()

                if (principal != null) {
                    val providerName = call.application.locations.resolve<LoginCallback>(LoginCallback::class, call).provider
                    val identityProvider = identityProviders[providerName]
                    if (identityProvider != null) {
                        val socialIdentity = identityProvider.getIdentity(principal.accessToken)

                        // TODO should handle errors from this call as well

                        val user = database.invoke<User> {
                            val query = select(Identity::class) where(Identity::provider eq providerName) and (Identity::id eq socialIdentity.id)
                            val identity = query.get().firstOrNull()

                            if (identity != null) {
                                identity.user
                            } else {
                                val user = (select(User::class) where (User::email eq socialIdentity.email)).get().firstOrNull()

                                if (user != null) {
                                    val newIdentity = IdentityEntity()
                                    newIdentity.provider = providerName
                                    newIdentity.id = socialIdentity.id
                                    newIdentity.user = user

                                    insert(newIdentity)

                                    user
                                } else {
                                    val newUser = UserEntity()

                                    newUser.name = socialIdentity.name
                                    newUser.nickname = socialIdentity.nickname
                                    newUser.email = socialIdentity.email
                                    newUser.avatarUrl = socialIdentity.avatar

                                    insert(newUser)

                                    val newIdentity = IdentityEntity()

                                    newIdentity.id = socialIdentity.id
                                    newIdentity.provider = providerName
                                    newIdentity.user = newUser

                                    insert(newIdentity)

                                    newUser
                                }
                            }
                        }
                    } else {
                        call.application.log.error("Unable to find identity provider configuration for $providerName")
                        call.respondView(LoginFailure(listOf("Unable to get user information from the login provider")))
                    }
                    call.respondView(LoginSuccess(principal))
                } else {
                    call.respondView(LoginFailure(listOf("Unable to find principal")))
                }
            }
        }
    }
}