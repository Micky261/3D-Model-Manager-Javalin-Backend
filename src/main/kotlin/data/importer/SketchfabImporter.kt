package data.importer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonNode
import core.httpclient.HttpClient
import data.bean.FileType
import data.bean.Model
import data.bean.ModelFileType
import data.bean.ModelTag
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import utils.getFilenameWithExtensionFromUrl
import utils.getText

@JsonIgnoreProperties(ignoreUnknown = true)
class Metadata(
    val license: JsonNode,
    val name: String,
    val tags: JsonNode,
    val thumbnails: JsonNode,
    val user: JsonNode,
    val viewerUrl: String,
    val isDownloadable: Boolean,
    val description: String,
)

class SketchfabImporter : BaseImporter() {
    private val baseUrl = "https://api.sketchfab.com/v3/"

    override fun import(userId: Long, args: Map<String, String>): Long {
        val id = args["id"]
        val personalApiKey = config.config.importer.sketchfab?.apiKey ?: ""

        val metadata: Metadata = runBlocking {
            HttpClient.instance.get(baseUrl + "models/$id") {
                headers { append(HttpHeaders.Authorization, "Token $personalApiKey") }
            }.body()
        }

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
            val modelTag = ModelTag(userId, modelId, tag.getText("name"))
            this.modelTagsService.insert(modelTag)
        }

        metadata.thumbnails.get("images").maxByOrNull { it.get("width").intValue() }?.also { image ->
            storeFile(
                image.getText("url"),
                userId,
                modelId,
                ModelFileType.image,
                image.getText("url").getFilenameWithExtensionFromUrl(),
                1L,
            )
        }

        if (metadata.isDownloadable) {
            val downloadLinks: JsonNode = runBlocking {
                HttpClient.instance.get(baseUrl + "models/$id/download") {
                    headers { append(HttpHeaders.Authorization, "Token $personalApiKey") }
                }.body()
            }

            downloadLinks.forEachIndexed { index, archiveLink ->
                val filename = archiveLink.getText("url").getFilenameWithExtensionFromUrl()
                storeFile(
                    archiveLink.getText("url"),
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
