package backend.collections

import core.javalin.ControllerModule
import core.javalin.JavalinRole
import io.javalin.apibuilder.ApiBuilder.delete
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.apibuilder.ApiBuilder.put

object CollectionModule : ControllerModule() {
    override fun configure() {
        route<CollectionController> { controller ->
            path("collections/") {
                get("", controller::getCollections, JavalinRole.Authorized)
                get("models/{modelId}", controller::getCollectionsOfModel, JavalinRole.ModelOwnerOnly)
            }

            path("collection/") {
                get("data/{id}", controller::getCollection, JavalinRole.Authorized)
                post("data", controller::createCollection, JavalinRole.Authorized)
                put("data/{id}", controller::updateCollection, JavalinRole.Authorized)
                delete("{id}", controller::deleteCollection, JavalinRole.Authorized)

                post("relation/{id}/{modelId}", controller::createRelation, JavalinRole.Authorized)
                delete("relation/{id}/{modelId}", controller::deleteRelation, JavalinRole.Authorized)

                get("models/{id}", controller::getModelsInCollection, JavalinRole.Authorized)
            }
        }
    }
}
