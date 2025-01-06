package data.importer

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import core.config.JacksonModule
import data.bean.Model
import data.dto.UserSettingKey

class MakerWorldImporter : BaseImporter() {
    private val downloadUrl =
        "https://makerworld.com/api/v1/design-service/design/%s/model?modelType=all&type=download"

    override fun import(userId: Long, args: Map<String, String>): Long {
        val id = args["id"]

        // TODO: Scrap data

        val model = Model(
            -1,
            userId = userId,
            name = "TODO",
            importedName = "TODO",
            description = "TODO",
            importedDescription = "TODO",
            notes = "",
            favorite = false,
            author = "Makerworld",
            importedAuthor = "Makerworld",
            licence = "",
            importedLicence = "",
            importSource = "https://makerworld.com/de/models/$id",
        )
        val modelId = this.modelService.insert(model)

        val token = userSettingsService.getSetting(userId, UserSettingKey.MakerWorldSessionToken) ?: throw NotImplementedError("Implementation todo")
        val (_, _, response) = Fuel.get(downloadUrl.format(id)).header("Cookie", "token=$token").responseString()
        val resp = JacksonModule.mapper.readValue<JsonNode>(response.get())
        val download = resp.get("url").asText()

        unzipDownload(download, userId, modelId)

//        metadata.get("tags").forEach { tag ->
//            val modelTag = ModelTag(userId, modelId, tag.asText())
//            this.modelTagsService.insert(modelTag)
//        }

//        var imageCounter = 1L
//        metadata.get("illustrations").forEach { illustration ->
//            storeFile(
//                illustration.get("imageUrl").asText(),
//                userId,
//                modelId,
//                ModelFileType.image,
//                illustration.get("imageUrl").asText().split("/").last(),
//                imageCounter++,
//            )
//        }
//
//        metadata.get("blueprints").forEach { blueprint ->
//            storeFile(
//                blueprint.get("imageUrl").asText(),
//                userId,
//                modelId,
//                ModelFileType.image,
//                blueprint.get("imageUrl").asText().split("/").last() + ".png",
//                imageCounter++,
//            )
//        }

        return modelId
    }
}
