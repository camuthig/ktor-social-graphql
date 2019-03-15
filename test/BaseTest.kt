package org.camuthig

import com.typesafe.config.ConfigFactory
import io.ktor.config.HoconApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.withApplication
import org.camuthig.auth.authModule
import org.junit.After
import org.junit.Before
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin

interface BaseTest {
    @Before
    fun before() {
        startKoin(listOf(authModule))
    }

    @After
    fun after() {
        stopKoin()
    }

    /**
     * Create a test application engine, instantiating Koin and reading from the application.conf file
     */
    fun <R> withConfiguredTestApplication(test: TestApplicationEngine.() -> R): R {
        return withApplication(
            environment = createTestEnvironment({
                config = HoconApplicationConfig(ConfigFactory.load("application.conf"))
            }),
            test = test)
    }
}


