package org.camuthig

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.camuthig.auth.UserEntity
import org.camuthig.auth.config.JwtConfiguration
import org.camuthig.ktor.database
import org.junit.Test
import kotlin.test.*

class AuthTest: BaseTest {
    @Test
    fun `it should retrieve the authenticated user with a JWT`() {
       withConfiguredTestApplication {
           handleRequest {
               val user = UserEntity()
               user.id = 1
               user.name = "Test Tester"
               user.nickname = "Test"
               user.email = "test@test.com"

               database.invoke {
                   insert(user)
               }

                val call = handleRequest(HttpMethod.Get, "/me") {
                    addHeader("Authorization", "Bearer ${JwtConfiguration.makeToken(user)}")
                }

                call.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertTrue(response.content!!.contains("\"id\":${user.id}"))
                }
           }
       }
    }
}