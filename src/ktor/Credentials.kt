package org.camuthig.ktor

import io.ktor.application.Application
import org.camuthig.credentials.core.ClassLoaderCredentialsStore

object Credentials {
    private val config = ClassLoaderCredentialsStore(ClassLoader.getSystemClassLoader()).load()

    fun get(key: String, default: String = ""): String {
        if (config.hasPath(key)) {
            return config.getString(key)
        }

        return default
    }
}

fun Application.credentials(key: String, default: String = ""): String  = Credentials.get(key, default)
