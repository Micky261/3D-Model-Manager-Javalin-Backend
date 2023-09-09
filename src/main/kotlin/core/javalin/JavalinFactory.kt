package core.javalin

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.google.inject.Provider
import core.config.AppConfig
import data.bean.ModelFileType
import data.importer.ImportSource
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.json.JavalinJackson
import org.eclipse.jetty.http.HttpStatus
import org.slf4j.Logger
import utils.thumbnail.ThumbnailFormat
import java.time.Instant
import java.util.UUID

class JavalinFactory @Inject constructor(
    private val objectMapper: ObjectMapper,
    private val routes: Set<Routing<*>>,
    private val logger: Logger,
    private val appConfig: AppConfig,
    private val appAccessManager: AppAccessManager,
) : Provider<Javalin> {
    override fun get(): Javalin = Javalin.create {
        it.jsonMapper(JavalinJackson(objectMapper))
        it.http.brotliAndGzipCompression()

        it.router.apiBuilder {
            path("api") {
                routes.forEach(Routing<*>::call)
            }
        }

        it.validation.register(List::class.java) { param -> param.split(",") }
        it.validation.register(UUID::class.java) { param -> UUID.fromString(param) }
        it.validation.register(Instant::class.java) { param -> Instant.ofEpochMilli(param.toLong()) }
        it.validation.register(ModelFileType::class.java) { param -> ModelFileType.valueOf(param) }
        it.validation.register(ImportSource::class.java) { param -> ImportSource.valueOf(param) }
        it.validation.register(ThumbnailFormat::class.java) { param -> ThumbnailFormat.valueOf(param) }

        if (appConfig.config.general.enableCors) {
            it.bundledPlugins.enableCors { corsContainer ->
                corsContainer.addRule { corsConfig ->
                    if (appConfig.config.general.corsAllowedHosts.isEmpty()) {
                        corsConfig.anyHost()
                    } else {
                        appConfig.config.general.corsAllowedHosts.forEach { host -> corsConfig.allowHost(host) }
                    }

                    corsConfig.allowCredentials = true
                }
            }
        }
    }.exception(Exception::class.java) { exception, ctx ->
        logger.debug("An error occurred during a request", exception)
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500).result("Internal Server Error")
    }.beforeMatched { ctx ->
        appAccessManager.handle(ctx)
    }
}
