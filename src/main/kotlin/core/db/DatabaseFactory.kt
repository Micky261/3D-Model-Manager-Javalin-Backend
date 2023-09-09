package core.db

import com.google.inject.Inject
import com.google.inject.Provider
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import core.config.AppConfig
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.mariadb.jdbc.MariaDbDataSource
import org.postgresql.ds.PGSimpleDataSource

class DatabaseFactory @Inject constructor(
    private val args: AppConfig,
) : Provider<Jdbi> {
    private val dataSource = HikariDataSource(
        run {
            val config = HikariConfig()

            config.jdbcUrl =
                "jdbc:${args.config.database.db}://${args.config.database.host}:" +
                "${args.config.database.port}/${args.config.database.name}"
            config.username = args.config.database.username
            config.password = args.config.database.password

            when (args.config.database.db) {
                "mysql", "mariadb" -> config.addDataSourceProperty(
                    "dataSourceClassName",
                    MariaDbDataSource::class.java.canonicalName,
                )

                "postgresql" -> config.addDataSourceProperty(
                    "dataSourceClassName",
                    PGSimpleDataSource::class.java.canonicalName,
                )

                else -> throw Exception("Unexpected Database Type, currently supported: MySQL, MariaDB, PostgreSQL")
            }

            config.addDataSourceProperty("autoCommit", "false")
            config.addDataSourceProperty("useServerPrepStmts", "true")
            config.addDataSourceProperty("cachePrepStmts", "true")

            config
        },
    )

    override fun get(): Jdbi = Jdbi.create(dataSource).run {
        installPlugin(SqlObjectPlugin())
        installPlugin(KotlinPlugin())
        installPlugin(KotlinSqlObjectPlugin())

        if (args.config.database.db == "postgresql") {
            installPlugin(PostgresPlugin())
        }

        registerColumnMapper(MapColumnMapper())
        registerArgument(MapArgumentFactory())

        this
    }
}
