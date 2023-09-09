package data.importer

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import core.config.JacksonModule
import data.bean.FileType
import data.bean.Model
import data.bean.ModelFileType
import data.bean.ModelTag
import utils.authToken

class Metadata(
    val animationCount: Int,
    val categories: JsonNode,
    val commentCount: Int,
    val createdAt: String,
    val embedUrl: String,
    val faceCount: Int,
    val isAgeRestricted: Boolean,
    val license: JsonNode,
    val likeCount: Int,
    val name: String,
    val publishedAt: String,
    val soundCount: Int,
    val staffpickedAt: Int,
    val tags: JsonNode,
    val thumbnails: JsonNode,
    val uid: String,
    val uri: String,
    val user: JsonNode,
    val vertexCount: Int,
    val viewCount: Int,
    val viewerUrl: String,
    val isDownloadable: Boolean,
    val isProtected: Boolean,
    val description: String,
    val price: Float,
    val downloadCount: Int,
    val editorUrl: String,
    val status: JsonNode,
    val source: String,
    val hasCommentsDisabled: Boolean,
    val updatedAt: String,
    val pbrType: String,
    val textureCount: Int,
    val materialCount: Int,
)

class SketchfabImporter : BaseImporter() {
    private val baseUrl = "https://api.sketchfab.com/v3/"

    override fun import(userId: Long, args: Map<String, String>): Long {
        val id = args["id"]
        val personalApiKey = config.config.importer.sketchfab?.apiKey ?: ""

        val (_, _, responseMetadata) = Fuel.get(baseUrl + "models/$id")
            .authToken(personalApiKey).responseString()
        val metadata = JacksonModule.mapper.readValue<Metadata>(responseMetadata.get())

        val model = Model(
            -1,
            userId = userId,
            name = metadata.name,
            importedName = metadata.name,
            description = converter.convert(metadata.description),
            importedDescription = converter.convert(metadata.description),
            notes = "",
            favorite = false,
            author = metadata.user.get("username").toString(),
            importedAuthor = metadata.user.get("username").toString(),
            licence = metadata.license.get("fullName").toString(), // license?
            importedLicence = metadata.license.get("fullName").toString(),
            importSource = metadata.viewerUrl,
        )
        val modelId = this.modelService.insert(model)

        metadata.tags.forEach { tag ->
            val modelTag = ModelTag(userId, modelId, tag.get("name").asText())
            this.modelTagsService.insert(modelTag)
        }

        metadata.thumbnails.get("images").maxByOrNull { it.get("width").intValue() }?.also { image ->
            storeFile(
                image.get("url").asText(),
                userId,
                modelId,
                ModelFileType.image,
                image.get("url").asText().split("/").last(),
                1L,
            )
        }

        if (metadata.isDownloadable) {
            val (_, _, responseDownloadLink) = Fuel.get(baseUrl + "models/$id/download")
                .authToken(personalApiKey).responseString()

            val downloadLinks: JsonNode = JacksonModule.mapper.readValue<JsonNode>(responseDownloadLink.get())

            downloadLinks.forEachIndexed { index, archiveLink ->
                val filename = archiveLink.get("url").asText().split("/").last().split("?").first()
                storeFile(
                    archiveLink.get("url").asText(),
                    userId,
                    modelId,
                    FileType.getModelFileTypeFromFilename(filename) ?: ModelFileType.various,
                    filename,
                    index.toLong() + 1,
                )
            }
        }
        return modelId
    }
}
