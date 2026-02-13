package data.importer

import com.fasterxml.jackson.databind.JsonNode
import core.httpclient.HttpClient
import data.bean.FileType
import data.bean.Model
import data.bean.ModelFileType
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import utils.getText

class InstructablesImporter : BaseImporter() {
    private val baseUrl = "https://www.instructables.com/json-api/"

    override fun import(userId: Long, args: Map<String, String>): Long {
        val id = args["id"]
        val personalApiKey = config.config.importer.thingiverse?.apiKey ?: ""

        val metadata: JsonNode = runBlocking {
            HttpClient.instance.get(baseUrl + "getFiles?instructableId=" + id) {
                headers { append(HttpHeaders.Authorization, "Token $personalApiKey") }
            }.body()
        }

        val model = Model(
            -1,
            userId = userId,
            name = id.toString(),
            importedName = "",
            description = "",
            importedDescription = "",
            notes = "",
            favorite = false,
            author = "",
            importedAuthor = "",
            licence = "",
            importedLicence = "",
            importSource = "",
        )

        val modelId = this.modelService.insert(model)

        var imageCounter = 1L
        var diagramCounter = 1L
        var documentCounter = 1L
        var modelCounter = 1L
        var slicedCounter = 1L
        var variousCounter = 1L

        metadata.get("files").forEach { filesPerStep ->
            filesPerStep.forEach { file ->
                val url = file.getText("downloadUrl")
                val filename = file.getText("name")
                val modelFileType = FileType.getModelFileTypeFromFilename(filename) ?: ModelFileType.various

                storeFile(
                    url,
                    userId,
                    modelId,
                    modelFileType,
                    filename,
                    when (modelFileType) {
                        ModelFileType.image -> imageCounter++
                        ModelFileType.diagram -> diagramCounter++
                        ModelFileType.document -> documentCounter++
                        ModelFileType.sliced -> slicedCounter++
                        ModelFileType.model -> modelCounter++
                        ModelFileType.various -> variousCounter++
                        else -> 999L
                    },
                )
            }
        }

        return modelId
    }
}
