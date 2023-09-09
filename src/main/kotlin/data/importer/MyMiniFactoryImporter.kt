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
            name = metadata.get("name").asText(),
            importedName = metadata.get("name").asText(),
            description = converter.convert(metadata.get("description_html").asText()),
            importedDescription = converter.convert(metadata.get("description_html").asText()),
            notes = "",
            favorite = false,
            author = metadata.get("designer").get("name").asText(),
            importedAuthor = metadata.get("designer").get("name").asText(),
            licence = metadata.get("license").asText(),
            importedLicence = metadata.get("license").asText(),
            importSource = metadata.get("url").asText(),
        )
        val modelId = this.modelService.insert(model)

        metadata.get("tags").forEach { tag ->
            val modelTag = ModelTag(userId, modelId, tag.asText())
            this.modelTagsService.insert(modelTag)
        }

        metadata.get("images").forEachIndexed { index, imageFile ->
            storeFile(
                imageFile.get("original").get("url").asText(),
                userId,
                modelId,
                ModelFileType.image,
                imageFile.get("original").get("url").asText().split("/").last(),
                index.toLong() + 1,
            )
        }

        metadata.get("files").get("items")
            // Map to filenames
            .map { it.get("filename").asText() }
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
