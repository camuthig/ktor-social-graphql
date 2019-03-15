package org.camuthig

import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*

class ApplicationTest: BaseTest() {
    @Test
    fun testRoot() {
        withConfiguredTestApplication {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("HELLO WORLD!", response.content)
            }
        }
    }
}
