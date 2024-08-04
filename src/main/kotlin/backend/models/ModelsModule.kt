package backend.models

import core.javalin.ControllerModule
import core.javalin.JavalinRole
import io.javalin.apibuilder.ApiBuilder.delete
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.apibuilder.ApiBuilder.put

object ModelsModule : ControllerModule() {
    override fun configure() {
        route<ModelsController> { controller ->
            path("models/") {
                get("", controller::getAllModels, JavalinRole.Authorized)
                get("with-tags", controller::getAllModelsWithTags, JavalinRole.Authorized)
                get("random/{num}", controller::getRandomModels, JavalinRole.Authorized)
                get("newest/{num}", controller::getNewestModels, JavalinRole.Authorized)
            }
        }

        route<ModelController> { controller ->
            path("model/") {
                get("data/{modelId}", controller::getModel, JavalinRole.ModelOwnerOnly)
                post("data", controller::createModel, JavalinRole.Authorized)
                put("data/{modelId}", controller::updateModel, JavalinRole.ModelOwnerOnly)
                delete("{modelId}", controller::deleteModel, JavalinRole.ModelOwnerOnly)
            }
        }

        route<ModelLinkController> { controller ->
            path("links/") {
                get("model/{modelId}", controller::getLinksForModel, JavalinRole.ModelOwnerOnly)
                post("model/{modelId}", controller::addLink, JavalinRole.ModelOwnerOnly)
                put("{id}", controller::updateLink, JavalinRole.Authorized)
                delete("{id}", controller::deleteLink, JavalinRole.Authorized)
            }
        }

        route<ThumbnailController> { controller ->
            path("thumbnails/") {
                get("default", controller::getDefaultThumbnail, JavalinRole.Authorized)
                get("file/{fileId}", controller::getThumbnail, JavalinRole.Authorized)
                get("model/{modelId}/main", controller::getMainThumbnail, JavalinRole.ModelOwnerOnly)
            }
        }
    }
}
