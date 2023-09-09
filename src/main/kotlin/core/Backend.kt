package core

import com.google.inject.Guice
import core.background.CleanUpCoroutine
import core.config.AppConfig
import core.db.DatabaseMigration
import dev.misfitlabs.kotlinguice4.getInstance
import io.javalin.Javalin
import io.javalin.http.BadGatewayResponse
import org.eclipse.jetty.http.HttpStatus.INTERNAL_SERVER_ERROR_500
import org.slf4j.Logger

fun main() {
    val injector = Guice.createInjector(BackendModule())
    val args = injector.getInstance<AppConfig>()

    val javalin = injector.getInstance<Javalin>()
    val logger = injector.getInstance<Logger>()

    injector.getInstance<DatabaseMigration>().migrate()

    javalin.exception(BadGatewayResponse::class.java) { exception, ctx ->
        if (exception.status >= 500) {
            logger.error("An error occurred during a request", exception)
        }
        ctx.status(exception.status)
    }.exception(Exception::class.java) { exception, ctx ->
        logger.info("An error occurred during a request", exception)
        ctx.status(INTERNAL_SERVER_ERROR_500).result("Internal Server Error")
    }.start(
        args.config.general.serverPort,
    )

    CleanUpCoroutine.start()
}
