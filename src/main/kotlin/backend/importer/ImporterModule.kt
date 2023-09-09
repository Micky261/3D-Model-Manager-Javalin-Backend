package backend.importer

import core.javalin.ControllerModule
import core.javalin.JavalinRole
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post

object ImporterModule : ControllerModule() {
    override fun configure() {
        route<ImporterController> { controller ->
            path("import/") {
                get("enabled", controller::getEnabled, JavalinRole.Authorized)
                post("{importer}", controller::importModel, JavalinRole.Authorized)
            }
        }
    }
}
