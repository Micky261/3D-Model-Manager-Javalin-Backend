package data.importer

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import core.config.JacksonModule
import data.bean.Model
import data.bean.ModelFileType
import data.bean.ModelTag

class Cults3DImporter : BaseImporter() {
    private val graphqlUrl = "https://cults3d.com/graphql"
    private val objectQuery =
        "{\"query\":\"{ creation(slug: \\\"%s\\\") { name(locale: EN) description(locale: EN) creator { nick } " +
            "license { name(locale: EN) } tags(locale: EN) url(locale: EN) illustrations { imageUrl } " +
            "blueprints { imageUrl fileUrl fileExtension } } } \",\"variables\":null}"

    override fun import(userId: Long, args: Map<String, String>): Long {
        val id = args["id"]
        val username = config.config.importer.cults3d?.username ?: ""
        val password = config.config.importer.cults3d?.password ?: ""

        val profileQuery = objectQuery.format(id)

        val (_, _, responseMetadata) = Fuel.post(graphqlUrl).jsonBody(profileQuery).authentication()
            .basic(username, password).responseString()
        val metadata = JacksonModule.mapper.readValue<JsonNode>(responseMetadata.get()).get("data").get("creation")

        val model = Model(
            -1,
            userId = userId,
            name = metadata.get("name").asText(),
            importedName = metadata.get("name").asText(),
            description = converter.convert(metadata.get("description").asText()),
            importedDescription = converter.convert(metadata.get("description").asText()),
            notes = "",
            favorite = false,
            author = metadata.get("creator")?.get("nick")!!.asText(),
            importedAuthor = metadata.get("creator")?.get("nick")!!.asText(),
            licence = metadata.get("license").get("name").asText(),
            importedLicence = metadata.get("license").get("name").asText(),
            importSource = metadata.get("url").asText(),
        )
        val modelId = this.modelService.insert(model)

        metadata.get("tags").forEach { tag ->
            val modelTag = ModelTag(userId, modelId, tag.asText())
            this.modelTagsService.insert(modelTag)
        }

        var imageCounter = 1L
        metadata.get("illustrations").forEach { illustration ->
            storeFile(
                illustration.get("imageUrl").asText(),
                userId,
                modelId,
                ModelFileType.image,
                illustration.get("imageUrl").asText().split("/").last(),
                imageCounter++,
            )
        }

        metadata.get("blueprints").forEach { blueprint ->
            storeFile(
                blueprint.get("imageUrl").asText(),
                userId,
                modelId,
                ModelFileType.image,
                blueprint.get("imageUrl").asText().split("/").last() + ".png",
                imageCounter++,
            )
        }

        return modelId
    }
}
