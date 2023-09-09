package core

import backend.BackendModule
import com.google.inject.Singleton
import core.config.AppConfig
import core.config.JacksonModule
import core.db.DatabaseFactory
import core.javalin.AppAccessManager
import core.javalin.JavalinFactory
import data.DaoModule
import data.ServiceModule
import dev.misfitlabs.kotlinguice4.KotlinModule
import io.github.cdimascio.dotenv.Dotenv
import io.javalin.Javalin
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BackendModule : KotlinModule() {
    private val logger = LoggerFactory.getLogger("3DMM") ?: error("Could not create Logger")

    override fun configure() {
        bind<Dotenv>().toInstance(Dotenv.configure().ignoreIfMissing().load())
        bind<AppConfig>().`in`<Singleton>()
        bind<Jdbi>().toProvider<DatabaseFactory>().`in`<Singleton>()
        bind<Logger>().toInstance(logger)

        install(JacksonModule)
        install(DaoModule)
        install(ServiceModule)
        install(BackendModule)

        bind<AppAccessManager>()

        bind<Javalin>().toProvider<JavalinFactory>().`in`<Singleton>()
    }
}
