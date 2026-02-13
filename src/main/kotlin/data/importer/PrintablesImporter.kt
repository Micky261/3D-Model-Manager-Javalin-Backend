package data.importer

import com.fasterxml.jackson.databind.JsonNode
import core.httpclient.HttpClient
import data.bean.Model
import data.bean.ModelFileType
import data.bean.ModelTag
import io.javalin.http.BadRequestResponse
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import utils.getText

class PrintablesImporter : BaseImporter() {
    private val modelBaseUrl = "https://www.printables.com/model/"
    private val mediaUrl = "https://media.printables.com/"
    private val graphqlUrl = "https://api.printables.com/graphql/"
    private val filesUrl = "https://files.printables.com/"

    private val profileQuery: String =
        """
            {"operationName": "PrintProfile","variables": {"id": "%d"},"query": "query PrintProfile(${"$"}id: ID!) { print(id: ${"$"}id) { ...PrintDetailFragment __typename } } fragment PrintDetailFragment on PrintType { id name user { publicUsername __typename } description category { id name path { id name description __typename } __typename } modified firstPublish datePublished dateCreatedThingiverse summary pdfFilePath images { ...ImageSimpleFragment __typename } tags { name id __typename } thingiverseLink license { id name abbreviation disallowRemixing __typename } gcodes { id name fileSize filePreviewPath __typename } stls { id name fileSize filePreviewPath __typename } slas { id name fileSize filePreviewPath __typename } __typename } fragment ImageSimpleFragment on PrintImageType { id filePath rotation __typename } "}
        """.trimIndent()
    private val downloadQuery: String =
        """
            {"operationName": "GetDownloadLink","query": "mutation GetDownloadLink(${"$"}id: ID!, ${"$"}modelId: ID!, ${"$"}fileType: DownloadFileTypeEnum!, ${"$"}source: DownloadSourceEnum!) {\n  getDownloadLink(\n    id: ${"$"}id\n    printId: ${"$"}modelId\n    fileType: ${"$"}fileType\n    source: ${"$"}source\n  ) {\n    ok\n    errors {\n      ...Error\n      __typename\n    }\n    output {\n      link\n      count\n      ttl\n      __typename\n    }\n    __typename\n  }\n}\nfragment Error on ErrorType {\n  field\n  messages\n  __typename\n}","variables": {"fileType": "%s","id": "%d","modelId": "%d","source": "model_detail"}}
        """.trimIndent()

    override fun import(userId: Long, args: Map<String, String>): Long {
        val id = args["id"]?.toLong() ?: throw BadRequestResponse()

        val profileQueryBody = profileQuery.format(id)

        val metadata: JsonNode = runBlocking {
            HttpClient.instance.post(graphqlUrl) {
                contentType(ContentType.Application.Json)
                setBody(profileQueryBody)
            }.body<JsonNode>().get("data").get("print")
        }

        val model = Model(
            -1,
            userId = userId,
            name = metadata.getText("name"),
            importedName = metadata.getText("name"),
            description = converter.convert(metadata.getText("description")),
            importedDescription = converter.convert(metadata.getText("description")),
            notes = "",
            favorite = false,
            author = metadata.get("user").getText("publicUsername"),
            importedAuthor = metadata.get("user").getText("publicUsername"),
            licence = metadata.get("license").getText("abbreviation"),
            importedLicence = metadata.get("license").getText("abbreviation"),
            importSource = modelBaseUrl + id,
        )

        val modelId = this.modelService.insert(model)

        metadata.get("tags").forEach { tag ->
            val modelTag = ModelTag(userId, modelId, tag.getText("name"))
            this.modelTagsService.insert(modelTag)
        }

        storeFile(
            filesUrl + metadata.getText("pdfFilePath"),
            userId,
            modelId,
            ModelFileType.document,
            "printables.pdf",
            1,
        )

        metadata.get("images").forEachIndexed { index, imageFile ->
            val imageFilePathString = imageFile.getText("filePath")
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
        metadata.get("gcodes").forEach { file ->
            storeFile(
                getDownloadUrl("gcode", file.get("id").asLong(), id), // mediaUrl + file.getText("filePath"),
                userId,
                modelId,
                ModelFileType.sliced,
                file.getText("name"),
                slicedCounter++,
            )
        }

        metadata.get("slas").forEach { file ->
            storeFile(
                getDownloadUrl("sla", file.get("id").asLong(), id), //   mediaUrl + file.getText("filePath"),
                userId,
                modelId,
                ModelFileType.sliced,
                file.getText("name"),
                slicedCounter++,
            )
        }

        metadata.get("stls").forEachIndexed { index, file ->
            storeFile(
                getDownloadUrl("stl", file.get("id").asLong(), id), //  mediaUrl + file.getText("filePath"),
                userId,
                modelId,
                ModelFileType.model,
                file.getText("name"),
                index.toLong() + 1,
            )
        }
        return modelId
    }

    private fun getDownloadUrl(type: String, fileId: Long, modelId: Long): String = runBlocking {
        val downloadQueryBody = downloadQuery.format(type, fileId, modelId)

        val response: JsonNode = HttpClient.instance.post(graphqlUrl) {
            contentType(ContentType.Application.Json)
            setBody(downloadQueryBody)
        }.body()
        response.get("data").get("getDownloadLink").get("output").getText("link")
    }
}
