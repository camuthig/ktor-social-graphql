package org.camuthig.auth

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.camuthig.BaseTest
import org.camuthig.auth.config.JwtConfiguration
import org.camuthig.auth.repository.InMemoryRepository
import org.junit.Before
import org.junit.Test
import org.koin.dsl.module.module
import org.koin.standalone.inject
import org.koin.test.KoinTest
import org.koin.test.declare
import kotlin.test.*

class AuthTest: BaseTest {
    @Before
    fun overrideModules() {
        declare {
            module {
                single<UserRepository> {
                    InMemoryRepository()
                }
            }
        }
    }

    @Test
    fun `it should retrieve the authenticated user with a JWT`() {
       withConfiguredTestApplication {
           handleRequest {
               val user = UserEntity()
               user.name = "Test Tester"
               user.nickname = "Test"
               user.email = "test@test.com"

               val repository: UserRepository by inject()

               repository.addUser(user)

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