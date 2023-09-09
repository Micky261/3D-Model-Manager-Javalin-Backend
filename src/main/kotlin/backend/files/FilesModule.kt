package backend.files

import core.javalin.ControllerModule
import core.javalin.JavalinRole
import io.javalin.apibuilder.ApiBuilder.delete
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post

object FilesModule : ControllerModule() {
    override fun configure() {
        route<FilesController> { controller ->
            path("files") {
                get("{modelId}", controller::getFiles, JavalinRole.ModelOwnerOnly)
                get("{modelId}/{type}", controller::getFilesWithType, JavalinRole.ModelOwnerOnly)
                post("{modelId}", controller::updateFiles, JavalinRole.ModelOwnerOnly)
            }

            path("zip") {
                get("{modelId}/{type}", controller::downloadZipFile, JavalinRole.ModelOwnerOnly)
            }

            path("file") {
                get("main/{modelId}", controller::getMainImage, JavalinRole.ModelOwnerOnly)
                get("single/{fileId}", controller::getFile, JavalinRole.Authorized)
                post("{modelId}", controller::saveFile, JavalinRole.ModelOwnerOnly)
                delete("{fileId}", controller::deleteFile, JavalinRole.Authorized)
            }
        }
    }
}
