package core.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import core.config.bean.AppConfigObj
import java.io.File

open class AppConfig @Inject constructor(mapper: ObjectMapper) {
    val config = mapper.readValue<AppConfigObj>(File("config/app-config.json"))

    fun getDataSourceDriver(): String {
        return when (config.database.db) {
            "mysql", "mariadb" -> "org.mariadb.jdbc.MariaDbDataSource"
            "postgresql" -> "org.postgresql.ds.PGSimpleDataSource"
            else -> throw Exception("Unexpected Database Type, currently supported: MySQL, MariaDB, PostgreSQL")
        }
    }
}
