package data.importer

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import core.config.JacksonModule
import data.bean.Model
import data.bean.ModelFileType
import data.bean.ModelTag
import utils.authToken

class ThingiverseImporter : BaseImporter() {
    private val baseUrl = "https://api.thingiverse.com/"

    override fun import(userId: Long, args: Map<String, String>): Long {
        val id = args["id"]
        val personalApiKey = config.config.importer.thingiverse?.apiKey ?: ""

        val (_, _, responseMetadata) = Fuel.get(baseUrl + "things/$id")
            .authToken(personalApiKey).responseString()
        val metadata: JsonNode = JacksonModule.mapper.readValue<JsonNode>(responseMetadata.get())

        val model = Model(
            -1,
            userId = userId,
            name = metadata.get("name")!!.asText(),
            importedName = metadata.get("name")!!.asText(),
            description = converter.convert(metadata.get("description_html")!!.asText()) + "\n\n" +
                converter.convert(metadata.get("instructions_html").asText()),
            importedDescription = converter.convert(metadata.get("description_html")!!.asText()) + "\n\n" +
                converter.convert(metadata.get("instructions_html").asText()),
            notes = "",
            favorite = false,
            author = metadata.get("creator")?.get("name")!!.asText(),
            importedAuthor = metadata.get("creator")?.get("name")!!.asText(),
            licence = metadata.get("license").asText(),
            importedLicence = metadata.get("license").asText(),
            importSource = metadata.get("public_url").asText(),
        )
        val modelId = this.modelService.insert(model)

        metadata.get("tags").forEach { tag ->
            val modelTag = ModelTag(userId, modelId, tag.get("name").asText())
            this.modelTagsService.insert(modelTag)
        }

        val (_, _, imageLinksResponse) = Fuel.get(baseUrl + "things/$id/images")
            .authToken(personalApiKey).responseString()

        JacksonModule.mapper.readValue<JsonNode>(imageLinksResponse.get()).forEachIndexed { index, fileDownloadLink ->
            fileDownloadLink.get("sizes")
                .first { it.get("type").asText() == "display" && it.get("size").asText() == "large" }
                .also { image ->
                    storeFile(
                        image.get("url").asText(),
                        userId,
                        modelId,
                        ModelFileType.image,
                        image.get("url").textValue().split("/").last(),
                        index.toLong() + 1,
                    )
                }
        }

        val (_, _, fileLinksResponse) = Fuel.get(baseUrl + "things/$id/files")
            .authToken(personalApiKey).responseString()

        JacksonModule.mapper.readValue<JsonNode>(fileLinksResponse.get()).forEachIndexed { index, fileDownloadLink ->
            storeFile(
                fileDownloadLink.get("public_url").asText(),
                userId,
                modelId,
                ModelFileType.model,
                fileDownloadLink.get("name").asText(),
                index.toLong() + 1,
            )
        }

        return modelId
    }
}
