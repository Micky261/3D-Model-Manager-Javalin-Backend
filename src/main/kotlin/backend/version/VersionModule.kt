package backend.version

import core.javalin.ControllerModule
import io.javalin.apibuilder.ApiBuilder.get

object VersionModule : ControllerModule() {
    override fun configure() {
        route<VersionController> { controller ->
            get("version", controller::getVersion)
        }
    }
}
