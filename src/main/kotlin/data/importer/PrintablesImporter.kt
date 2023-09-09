package data.importer

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import core.config.JacksonModule
import data.bean.Model
import data.bean.ModelFileType
import data.bean.ModelTag
import io.javalin.http.BadRequestResponse

class PrintablesImporter : BaseImporter() {
    private val modelBaseUrl = "https://www.printables.com/model/"
    private val mediaUrl = "https://media.printables.com/"
    private val graphqlUrl = "https://api.printables.com/graphql/"
    // private val filesUrl = "https://files.printables.com/"

    private val profileQuery: String =
        """
            {"operationName": "PrintProfile","variables": {"id": "%d"},"query": "query PrintProfile(${"$"}id: ID!) { print(id: ${"$"}id) { ...PrintDetailFragment __typename } } fragment PrintDetailFragment on PrintType { id name user { publicUsername __typename } description category { id name path { id name description __typename } __typename } modified firstPublish datePublished dateCreatedThingiverse summary pdfFilePath images { ...ImageSimpleFragment __typename } tags { name id __typename } thingiverseLink license { id name abbreviation disallowRemixing __typename } gcodes { id name filePath fileSize filePreviewPath __typename } stls { id name filePath fileSize filePreviewPath __typename } slas { id name filePath fileSize filePreviewPath __typename } __typename } fragment ImageSimpleFragment on PrintImageType { id filePath rotation __typename } "}
        """.trimIndent()

    override fun import(userId: Long, args: Map<String, String>): Long {
        val id = args["id"]?.toLong() ?: throw BadRequestResponse()

        val profileQuery = profileQuery.format(id)

        val (_, _, responseMetadata) = Fuel.post(graphqlUrl).jsonBody(profileQuery).responseString()
        val metadata: JsonNode = JacksonModule.mapper.readValue<JsonNode>(responseMetadata.get())
            .get("data").get("print")

        val model = Model(
            -1,
            userId = userId,
            name = metadata.get("name").asText(),
            importedName = metadata.get("name").asText(),
            description = converter.convert(metadata.get("description").asText()),
            importedDescription = converter.convert(metadata.get("description").asText()),
            notes = "",
            favorite = false,
            author = metadata.get("user").get("publicUsername").asText(),
            importedAuthor = metadata.get("user").get("publicUsername").asText(),
            licence = metadata.get("license").get("abbreviation").asText(),
            importedLicence = metadata.get("license").get("abbreviation").asText(),
            importSource = modelBaseUrl + id,
        )

        val modelId = this.modelService.insert(model)

        metadata.get("tags").forEach { tag ->
            val modelTag = ModelTag(userId, modelId, tag.get("name").asText())
            this.modelTagsService.insert(modelTag)
        }

        metadata.get("images").forEachIndexed { index, imageFile ->
            val imageFilePathString = imageFile.get("filePath").asText()
            storeFile(
                mediaUrl + imageFilePathString,
                userId,
                modelId,
                ModelFileType.image,
                imageFilePathString.split("/").last(),
                index.toLong() + 1,
            )
        }

        var slicedCounter = 1L
        metadata.get("gcodes").forEach { gcodeFile ->
            storeFile(
                mediaUrl + gcodeFile.get("filePath").asText(),
                userId,
                modelId,
                ModelFileType.sliced,
                gcodeFile.get("name").asText(),
                slicedCounter++,
            )
        }
        metadata.get("slas").forEach { slaFile ->
            storeFile(
                mediaUrl + slaFile.get("filePath").asText(),
                userId,
                modelId,
                ModelFileType.sliced,
                slaFile.get("name").asText(),
                slicedCounter++,
            )
        }

        metadata.get("stls").forEachIndexed { index, stlFile ->
            storeFile(
                mediaUrl + stlFile.get("filePath").asText(),
                userId,
                modelId,
                ModelFileType.model,
                stlFile.get("name").asText(),
                index.toLong() + 1,
            )
        }
        return modelId
    }
}
