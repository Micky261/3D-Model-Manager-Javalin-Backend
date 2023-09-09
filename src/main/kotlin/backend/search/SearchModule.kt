package backend.search

import core.javalin.ControllerModule
import core.javalin.JavalinRole
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path

object SearchModule : ControllerModule() {
    override fun configure() {
        route<SearchController> { controller ->
            path("search/") {
                get("models/{term}/{fields}", controller::search, JavalinRole.Authorized)
            }
        }
    }
}
