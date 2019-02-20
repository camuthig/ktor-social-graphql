package org.camuthig

import com.typesafe.config.ConfigFactory
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.content.*
import io.ktor.http.content.*
import io.ktor.auth.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import kotlin.test.*
import io.ktor.server.testing.*
import org.apache.commons.codec.binary.Hex
import org.camuthig.ktor.EncryptConfiguration
import java.util.*
import javax.crypto.spec.SecretKeySpec

class EncryptedConfigurationTest {
    @Test
    fun `it should decrypt an encrypted config`() {
        val encrypted = Base64.getDecoder().decode("Sx86jN1IRpN6epiTS/Ct6ZmeN7XP3kWtRP116TKLnLMPsxh9dUzEU2RFd3qDCjGT")
        val key = "thisisnotarealmasterkeybutitwork"
        val expected = """
            ktor {
              port: 8080
            }

            app {
              test: one
            }

        """.trimIndent()

        val config = EncryptConfiguration.decryptConfiguration(encrypted, SecretKeySpec(key.toByteArray(), "AES"))

        assertEquals(expected, config)
    }
}