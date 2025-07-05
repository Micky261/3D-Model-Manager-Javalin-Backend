package data.importer

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import core.config.JacksonModule
import data.bean.Model
import data.bean.ModelFileType
import data.bean.ModelTag
import io.javalin.http.BadRequestResponse
import utils.authToken
import utils.getText

class MyMiniFactoryImporter : BaseImporter() {
    private val fileUrl = "https://www.myminifactory.com/download/"
    private var baseUrl = "https://www.myminifactory.com/api/v2/"

    override fun import(userId: Long, args: Map<String, String>): Long {
        val id = args["id"]?.toLong() ?: throw BadRequestResponse()
        val personalApiKey = config.config.importer.myminifactory?.apiKey ?: ""

        val (_, _, responseMetadata) = Fuel.get(baseUrl + "objects/$id", listOf("key" to personalApiKey))
            .authToken(personalApiKey).responseString()
        val metadata: JsonNode = JacksonModule.mapper.readValue<JsonNode>(responseMetadata.get())

        val model = Model(
            -1,
            userId = userId,
            name = metadata.getText("name"),
            importedName = metadata.getText("name"),
            description = converter.convert(metadata.getText("description_html")),
            importedDescription = converter.convert(metadata.getText("description_html")),
            notes = "",
            favorite = false,
            author = metadata.get("designer").getText("name"),
            importedAuthor = metadata.get("designer").getText("name"),
            licence = metadata.getText("license"),
            importedLicence = metadata.getText("license"),
            importSource = metadata.getText("url"),
        )
        val modelId = this.modelService.insert(model)

        metadata.get("tags").forEach { tag ->
            val modelTag = ModelTag(userId, modelId, tag.asText())
            this.modelTagsService.insert(modelTag)
        }

        metadata.get("images").forEachIndexed { index, imageFile ->
            storeFile(
                imageFile.get("original").getText("url"),
                userId,
                modelId,
                ModelFileType.image,
                imageFile.get("original").getText("url").split("/").last(),
                index.toLong() + 1,
            )
        }

        metadata.get("files").get("items")
            // Map to filenames
            .map { it.getText("filename") }
            // Images are also in the files data -> Ignore
            .filterNot { filename -> filename.split(".").map { it.lowercase() }.equals("JPG") }
            .forEachIndexed { index, filename ->
                storeFile(
                    "$fileUrl$id?downloadfile=$filename",
                    userId,
                    modelId,
                    ModelFileType.model,
                    filename,
                    index.toLong() + 1,
                )
            }

        return modelId
    }
}
