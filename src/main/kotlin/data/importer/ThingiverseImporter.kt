package data.importer

import com.fasterxml.jackson.databind.JsonNode
import core.httpclient.HttpClient
import data.bean.Model
import data.bean.ModelFileType
import data.bean.ModelTag
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import utils.getText

class ThingiverseImporter : BaseImporter() {
    private val baseUrl = "https://api.thingiverse.com/"

    override fun import(userId: Long, args: Map<String, String>): Long {
        val id = args["id"]
        val personalApiKey = config.config.importer.thingiverse?.apiKey ?: ""

        val metadata: JsonNode = runBlocking {
            HttpClient.instance.get(baseUrl + "things/$id") {
                headers { append(HttpHeaders.Authorization, "Token $personalApiKey") }
            }.body()
        }

        val model = Model(
            -1,
            userId = userId,
            name = metadata.getText("name"),
            importedName = metadata.getText("name"),
            description = converter.convert(metadata.getText("description_html")) + "\n\n" +
                converter.convert(metadata.getText("instructions_html")),
            importedDescription = converter.convert(metadata.getText("description_html")) + "\n\n" +
                converter.convert(metadata.getText("instructions_html")),
            notes = "",
            favorite = false,
            author = metadata.get("creator").getText("name"),
            importedAuthor = metadata.get("creator").getText("name"),
            licence = metadata.getText("license"),
            importedLicence = metadata.getText("license"),
            importSource = metadata.get("public_url").asText(),
        )
        val modelId = this.modelService.insert(model)

        metadata.get("tags").forEach { tag ->
            val modelTag = ModelTag(userId, modelId, tag.getText("name"))
            this.modelTagsService.insert(modelTag)
        }

        val imageLinks: JsonNode = runBlocking {
            HttpClient.instance.get(baseUrl + "things/$id/images") {
                headers { append(HttpHeaders.Authorization, "Token $personalApiKey") }
            }.body()
        }

        imageLinks.forEachIndexed { index, fileDownloadLink ->
            val filename = fileDownloadLink.getText("name")

            fileDownloadLink.get("sizes")
                .first { it.getText("type") == "display" && it.getText("size") == "large" }
                .also { image ->
                    storeFile(
                        image.getText("url"),
                        userId,
                        modelId,
                        ModelFileType.image,
                        filename,
                        index.toLong() + 1,
                    )
                }
        }

        // val (_, _, fileLinksResponse) = Fuel.get(baseUrl + "things/$id/files")
        //    .authToken(personalApiKey).responseString()

        metadata.get("zip_data").get("files").forEachIndexed { index, fileDownloadLink ->
            storeFile(
                fileDownloadLink.getText("url"),
                userId,
                modelId,
                ModelFileType.model,
                fileDownloadLink.getText("name"),
                index.toLong() + 1,
            )
        }

        return modelId
    }
}
