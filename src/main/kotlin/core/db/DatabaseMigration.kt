package core.db

import com.google.inject.Inject
import core.config.AppConfig
import org.flywaydb.core.Flyway
import org.mariadb.jdbc.MariaDbDataSource
import org.postgresql.ds.PGSimpleDataSource

class DatabaseMigration @Inject constructor(
    private val args: AppConfig,
) {
    fun migrate() {
        Flyway.configure()
            .dataSource(
                when (args.config.database.db) {
                    "mysql", "mariadb" ->
                        MariaDbDataSource().apply {
                            url = "jdbc:${args.config.database.db}://${args.config.database.host}:" +
                                "${args.config.database.port}/${args.config.database.name}"
                            user = args.config.database.username
                            setPassword(args.config.database.password)
                        }

                    "postgresql" -> PGSimpleDataSource().apply {
                        serverNames = arrayOf(args.config.database.host)
                        portNumbers = arrayOf(args.config.database.port).toIntArray()
                        user = args.config.database.username
                        password = args.config.database.password
                        databaseName = args.config.database.name
                        currentSchema = args.config.database.name
                        isReWriteBatchedInserts = true
                    }

                    else -> throw Exception("Unexpected Database Type, currently supported: MySQL, MariaDB, PostgreSQL")
                },
            )
            .mixed(true)
            .validateOnMigrate(false)
            .baselineOnMigrate(true)
            .load()
            .migrate()
    }
}
