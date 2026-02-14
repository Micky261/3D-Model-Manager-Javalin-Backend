package data.importer

import com.fasterxml.jackson.databind.JsonNode
import core.httpclient.HttpClient
import data.bean.FileType
import data.bean.Model
import data.bean.ModelFileType
import data.bean.ModelTag
import io.javalin.http.FailedDependencyResponse
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import utils.getFilenameWithExtensionFromUrl
import utils.getText
import java.util.Base64

/**
 * This Importer is currently restricted to importing metadata only as order processes and downloads
 * are not available over the API (at the time of writing).
 * The only working approach would be forging of CSRF tokens, which is not ethical.
 * Therefore, this Importer will be restricted until the API is extended.
 *
 * Code to check for already made orders, starting the ordering process and downloading the files
 * is provided below as it was created during testing.
 * The frontend-side implementation to set the _session_id will be kept but hidden from the user.
 * The UserSettings key will remain available (although useless).
 */
class Cults3DImporter : BaseImporter() {
    private val graphqlUrl = "https://cults3d.com/graphql"
    private val objectQuery =
        "{\"query\":\"{ creation(slug: \\\"%s\\\") { name(locale: EN) description(locale: EN) " +
            "details(locale: EN) creator { nick } " +
            "license { name(locale: EN) } tags(locale: EN) url(locale: EN) illustrations { imageUrl } " +
            "blueprints { imageUrl fileUrl fileExtension } } } \",\"variables\":null}"

//    private val orderUrl = "https://cults3d.com/en/free_orders?creation_slug="
//    private val ordersQuery =
//        "{\"query\":\"{ myself { ordersBatch(sort: BY_CREATION, direction: DESC, limit: 100, offset: %s)" +
//            " { results { lines { downloadUrl creation { slug } } } } } } \",\"variables\":null}"

    override fun import(userId: Long, args: Map<String, String>): Long {
        val slug = args["id"]!!

        // Necessary prechecks
        val username = config.config.importer.cults3d?.username
            ?: throw FailedDependencyResponse("Cults3D Login data not provided in settings")
        val password = config.config.importer.cults3d.password
        // Only necessary for ordering process, see below
//        if (userSettingsService.getSetting(userId, UserSettingKey.Cults3dSessionId) == null) {
//            throw FailedDependencyResponse("Cults3dSessionId is not set")
//        }
        // Only necessary for auto download, see below
//        val orderLine = findOrder(username, password, slug)
//        println(orderLine)

        // Get metadata of model
        val profileQueryBody = objectQuery.format(slug)
        val basicAuth = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
        val metadata: JsonNode = runBlocking {
            HttpClient.instance.post(graphqlUrl) {
                contentType(ContentType.Application.Json)
                setBody(profileQueryBody)
                headers { append(HttpHeaders.Authorization, "Basic $basicAuth") }
            }.body<JsonNode>().get("data").get("creation")
        }

        // Process metadata
        val model = Model(
            -1,
            userId = userId,
            name = metadata.getText("name"),
            importedName = metadata.getText("name"),
            description = printDescription(metadata),
            importedDescription = printDescription(metadata),
            notes = "",
            favorite = false,
            author = metadata.get("creator").getText("nick"),
            importedAuthor = metadata.get("creator").getText("nick"),
            licence = metadata.get("license").getText("name"),
            importedLicence = metadata.get("license").getText("name"),
            importSource = metadata.getText("url"),
        )
        val modelId = this.modelService.insert(model)

        metadata.get("tags").forEach { tag ->
            val modelTag = ModelTag(userId, modelId, tag.asText())
            this.modelTagsService.insert(modelTag)
        }

        var imageCounter = 1L
        metadata.get("illustrations").forEach { illustration ->
            storeFile(
                illustration.getText("imageUrl"),
                userId,
                modelId,
                ModelFileType.image,
                illustration.getText("imageUrl").split("/").last(),
                imageCounter++,
            )
        }

        metadata.get("blueprints").forEach { blueprint ->
            storeFile(
                blueprint.getText("imageUrl"),
                userId,
                modelId,
                ModelFileType.image,
                FileType.imagineExtension(blueprint.getText("imageUrl").getFilenameWithExtensionFromUrl(), "png"),
                imageCounter++,
            )
        }

        // Not possible due to CSRF token
        /*
               // Start an ordering process to gain model files
         val (_, response, result) = Fuel.post(orderUrl + slug)
         .header(
         "Cookie",
         "_session_id=" + userSettingsService.getSetting(userId, UserSettingKey.Cults3dSessionId)
         )
         .responseString()

         if (response.statusCode !in 200..299) {
         throw InternalServerErrorResponse("Could not order model: $slug")
         }

         println(response) */

        // Not possible due to CSRF token
        @Suppress("standard:kdoc")
        /*        // Handle downloads
         var fileCounter = 1L
         orderLine.get("lines").forEach { line ->

         val body = Fuel.get(line.get("downloadUrl").asText())
         .header(
         "Cookie",
         "_session_id=" + userSettingsService.getSetting(userId, UserSettingKey.Cults3dSessionId)
         )
         .response().second
         val size = body.data.size.toLong()
         println("${body.contentLength} ${body.data.size} ${body.data[1]}")

         println(body)
         println(body.headers)
         println(body.headers["Content-Disposition"])
         println(body.headers["Content-Disposition"].toString())
         val finalFilename =
         body.headers["Content-Disposition"].toString().split(";")[1].split("=")[1].replace("\"", "")

         println(finalFilename)

         println("-----------IMPORTER---------------")

         storeFile(
         line.get("downloadUrl").asText(),
         userId,
         modelId,
         ModelFileType.model,
         line.get("creation").getText("slug"),
         fileCounter++,
         true
         )
         }*/

        return modelId
    }

    // Only necessary for auto download, see below

    /**    private fun findOrder(username: String, password: String, slug: String): JsonNode {
     var orderOffset = 0 // Increase by 100

     while (true) {
     val orderQuery = ordersQuery.format(orderOffset)
     val (_, _, response) = Fuel.post(graphqlUrl).jsonBody(orderQuery)
     .authentication().basic(username, password).responseString()
     val metadata = JacksonModule.mapper.readValue<JsonNode>(response.get())
     .get("data").get("myself").get("ordersBatch")

     if (metadata.get("results").isEmpty) {
     throw InternalServerErrorResponse("Could not find order for slug $slug")
     }

     val orderLine = metadata.get("results").firstOrNull { line ->
     line.get("lines").get(0).get("creation").getText("slug") == slug
     }

     if (orderLine != null) {
     return orderLine
     } else {
     orderOffset += 100
     }
     }
     }*/

    fun printDescription(metadata: JsonNode): String = if (!listOf("", "-").contains(metadata.getText("details"))) {
        """
        |${converter.convert(metadata.getText("description"))}
        |
        |## 3D Printing Settings
        |${converter.convert(metadata.getText("details"))}
        """.trimMargin()
    } else {
        "${converter.convert(metadata.getText("description"))}"
    }
}
