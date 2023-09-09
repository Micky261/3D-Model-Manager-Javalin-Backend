package backend.tags

import core.javalin.ControllerModule
import core.javalin.JavalinRole
import io.javalin.apibuilder.ApiBuilder.delete
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post

object TagsModule : ControllerModule() {
    override fun configure() {
        route<TagsController> { controller ->
            path("tags") {
                get("all", controller::getAllTags, JavalinRole.Authorized)
                get("model/{modelId}", controller::getModelTags, JavalinRole.ModelOwnerOnly)
                post("model/{modelId}/{tag}", controller::setModelTag, JavalinRole.ModelOwnerOnly)
                delete("model/{modelId}/{tag}", controller::removeModelTag, JavalinRole.ModelOwnerOnly)
            }
        }
    }
}
