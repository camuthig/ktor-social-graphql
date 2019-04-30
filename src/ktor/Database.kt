package org.camuthig.ktor

import io.requery.Persistable
import io.requery.sql.KotlinConfiguration
import io.requery.sql.KotlinEntityDataStore
import org.camuthig.Models
import org.postgresql.ds.PGSimpleDataSource

val database = createDataSource()

fun createDataSource(): KotlinEntityDataStore<Persistable> {

    val dataSource = PGSimpleDataSource()

    dataSource.setURL(Credentials.get("flyway.url"))
    dataSource.user = Credentials.get("flyway.user")
    dataSource.password = Credentials.get("flyway.password")

    val configuration = KotlinConfiguration(dataSource = dataSource, model = Models.DEFAULT, quoteColumnNames = true, useDefaultLogging = true)
    return KotlinEntityDataStore<Persistable>(configuration)
}
