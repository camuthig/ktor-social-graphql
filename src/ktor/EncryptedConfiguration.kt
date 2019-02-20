package org.camuthig.ktor

import com.typesafe.config.*
import java.io.*
import java.security.Key
import javax.crypto.CipherInputStream
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptConfiguration {
    fun getKey(loader: ClassLoader): SecretKey {
        val keyPath = System.getProperty("config.masterKey", "master.key")

        val resource =
            loader.getResource(keyPath) ?: throw ConfigException.Generic("The key at $keyPath is missing")

        return SecretKeySpec(File(resource.toURI()).readBytes(), "AES")
    }

    fun decryptConfiguration(configuration: ByteArray, key: Key): String {
        var content = ""
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        ByteArrayInputStream(configuration).use { inputFile ->
            // TODO Move this IV elsewhere, possibly in the master key file
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec("1234567890123456".toByteArray(),0, cipher.blockSize))

            CipherInputStream(inputFile, cipher).use { cipherIn ->
                InputStreamReader(cipherIn).use { inputReader ->
                    BufferedReader(inputReader).use { reader ->

                        val sb = StringBuilder()
                        reader.forEachLine { line ->
                            sb.append(line + "\n")
                        }

                        content = sb.toString()
                    }
                }
            }

            return content
        }
    }

    /**
     * A loading strategy to pull the configuration from an encrypted file on the system.
     */
    open class EncryptedConfigLoadingStrategy() : ConfigLoadingStrategy {
        override fun parseApplicationConfig(parseOptions: ConfigParseOptions): Config {
            val loader = parseOptions.classLoader ?: throw ConfigException.BugOrBroken(
                "ClassLoader should have been set here; bug in ConfigFactory. "
                        + "(You can probably work around this bug by passing in a class loader or calling currentThread().setContextClassLoader() though.)"
            )

            val key = getKey(loader)

            var specified = 0

            // override application.conf with config.file, config.resource,
            // config.url if requested.
            var resource = System.getProperty("config.resource")
            if (resource != null) {
                specified += 1
            }

            val file = System.getProperty("config.file")
            if (file != null) {
                specified += 1
            }

            // Skipping URL config file, keep things simple when encrypting

            if (specified == 0) {
                // Pulling the encrypted application file
                val decrypted =
                    decryptConfiguration(loader.getResource("application.encrypted").openStream().readAllBytes(), key)

                return ConfigFactory.parseString(decrypted, parseOptions)
            }

            if (specified > 1) {
                throw ConfigException.Generic(
                    "You set more than one of config.file='" + file
                            + "', config.resource='" + resource
                            + "'; don't know which one to use!"
                )
            }


            // the override file/resource MUST be present or it's an error
            val overrideOptions = parseOptions.setAllowMissing(false)

            if (resource != null) {
                if (resource.startsWith("/")) {
                    resource = resource.substring(1)
                }
                // this deliberately does not parseResourcesAnySyntax; if
                // people want that they can use an include statement.
                val decrypted = decryptConfiguration(loader.getResource(resource).openStream().readAllBytes(), key)

                return ConfigFactory.parseString(decrypted, overrideOptions)
            }

            if (file != null) {
                val decrypted = decryptConfiguration(File(file).readBytes(), key)

                return ConfigFactory.parseString(decrypted, overrideOptions)
            }

            throw ConfigException.BugOrBroken("A configuration file was not set.")
        }
    }
}
