package data.importer

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import core.config.JacksonModule
import data.bean.FileType
import data.bean.Model
import data.bean.ModelFileType
import utils.authToken

class InstructablesImporter : BaseImporter() {
    private val baseUrl = "https://www.instructables.com/json-api/"

    override fun import(userId: Long, args: Map<String, String>): Long {
        val id = args["id"]
        val personalApiKey = config.config.importer.thingiverse?.apiKey ?: ""

        val (_, _, responseMetadata) = Fuel.get(baseUrl + "getFiles?instructableId=" + id)
            .authToken(personalApiKey).responseString()
        val metadata: JsonNode = JacksonModule.mapper.readValue<JsonNode>(responseMetadata.get())

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
                val url = file.get("downloadUrl").asText()
                val filename = file.get("name").asText()
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
