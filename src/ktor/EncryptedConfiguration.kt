package org.camuthig.ktor

import com.typesafe.config.*
import java.io.*
import java.security.Key
import java.util.*
import javax.crypto.CipherInputStream
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptConfiguration {
    private val cipher:Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

    fun getKey(loader: ClassLoader): SecretKey {
        val keyPath = System.getProperty("config.masterKey", "master.key")

        val resource =
            loader.getResource(keyPath) ?: throw ConfigException.Generic("The key at $keyPath is missing")

        return SecretKeySpec(File(resource.toURI()).readBytes(), "AES")
    }

    fun getConfiguration(loader: ClassLoader): File {
        var specified = 0
        // override application.conf with config.file, config.resource,
        // config.url if requested.
        var resource = System.getProperty("config.encryptedResource")
        if (resource != null) {
            specified += 1
        }

        val file = System.getProperty("config.encryptedFile")
        if (file != null) {
            specified += 1
        }

        if (specified > 1) {
            throw ConfigException.Generic(
                "You set more than one of config.file='" + file
                        + "', config.resource='" + resource
                        + "'; don't know which one to use!"
            )
        }

        if (specified == 0) {
            return File(loader.getResource("application.encrypted").toURI())
        }

        if (resource != null) {
            if (resource.startsWith("/")) {
                resource = resource.substring(1)
            }

            return File(loader.getResource(resource).toURI())
        }

        if (file != null) {
            return File(file)
        }

        throw ConfigException.BugOrBroken("A configuration file was not set.")
    }



    fun decryptConfiguration(configuration: ByteArray, key: Key): String {
        var content = ""

        ByteArrayInputStream(configuration).use { inputFile ->
            // TODO Move this IV elsewhere, possibly in the master key file
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec("1234567890123456".toByteArray(),0, cipher.blockSize))

            CipherInputStream(inputFile, cipher).use { cipherIn ->
                InputStreamReader(cipherIn).use { inputReader ->
                    BufferedReader(inputReader).use { reader ->

                        val sb = StringBuilder()
                        reader.forEachLine { line ->
                            sb.appendln(line)
                        }

                        content = sb.toString()
                    }
                }
            }

            return content
        }
    }

    fun encryptConfiguration(configuration: StringBuffer, key: Key): ByteArray {
        val encrypted = ByteArrayOutputStream()
        // TODO Move this IV elsewhere, possibly in the master key file
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec("1234567890123456".toByteArray(),0, cipher.blockSize))

        CipherOutputStream(encrypted, cipher).bufferedWriter().use {cipherOut ->
            configuration.forEach { c ->
                cipherOut.append(c)
            }
        }

        return encrypted.toByteArray()
    }

    fun writeEncryptedFile(file: File, configuration: ByteArray) {
        // TODO This could be streamed with some alterations
        file.writeBytes(Base64.getEncoder().encode(configuration))
    }

    fun readEncryptedFile(loader: ClassLoader): ByteArray {
        val encoded = StringBuilder()
        getConfiguration(loader).forEachLine {
            encoded.appendln(it)
        }

        // TODO This could be streamed with some alterations
        return Base64.getDecoder().decode(encoded.toString())
    }

    private fun isUsingSpecifiedConfiguration(): Boolean {
        return System.getProperty("config.encryptedResource") != null || System.getProperty("config.encryptedFile") != null
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

            // the override file/resource MUST be present or it's an error
            val overrideOptions = if (isUsingSpecifiedConfiguration()) {
                parseOptions.setAllowMissing(false)
            } else {
                parseOptions
            }

            return ConfigFactory.parseString(decryptConfiguration(readEncryptedFile(loader), getKey(loader)), overrideOptions)
        }
    }
}
