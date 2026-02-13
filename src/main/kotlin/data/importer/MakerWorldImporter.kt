package data.importer

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import core.config.JacksonModule
import data.bean.FileType
import data.bean.Model
import data.bean.ModelFileType
import data.bean.ModelTag
import data.dto.UserSettingKey
import io.javalin.http.FailedDependencyResponse
import utils.getBoolean
import utils.getFilenameWithExtensionFromUrl
import utils.getLong
import utils.getText
import utils.ua

class MakerWorldImporter : BaseImporter() {
    private data class FileCounters(var model: Long = 1L, var various: Long = 1L)

    private val modelUrl =
        "https://makerworld.com/api/v1/design-service/design/%s"
    private val downloadUrlPostfix = "/model?modelType=all&type=download"

    // For 3mf ///// If stl: fileType=3mfstl
    private val instanceUrl =
        "https://makerworld.com/api/v1/design-service/instance/%s/f3mf?type=download&fileType="
    private val instanceFileTypes = listOf("", "3mfstl")

    /** TODO
     * Sometimes a captcha appears, this must be communicated to the user.
     * Cannot currently be triggered, therefore wait for error messages
     */

    override fun import(userId: Long, args: Map<String, String>): Long {
        val id = args["id"]

        val modelUrl = modelUrl.format(id)
        val downloadUrl = modelUrl + downloadUrlPostfix

        if (userSettingsService.getSetting(userId, UserSettingKey.MakerWorldSessionToken) == null) {
            throw FailedDependencyResponse("MakerWorldSessionToken is not set")
        }

        val (_, _, responseMetadata) = Fuel.get(modelUrl).ua()
            .header("Host", "makerworld.com")
            .responseString()
        val metadata: JsonNode = JacksonModule.mapper.readValue<JsonNode>(responseMetadata.get())

        val model = Model(
            -1,
            userId = userId,
            name = metadata.getText("title"),
            importedName = metadata.getText("title"),
            description = metadata.getText("summary"),
            importedDescription = metadata.getText("summary"),
            notes = "",
            favorite = false,
            author = metadata.get("designCreator").getText("name"),
            importedAuthor = metadata.get("designCreator").getText("name"),
            licence = metadata.getText("license"),
            importedLicence = metadata.getText("license"),
            importSource = "https://makerworld.com/models/$id-" + metadata.getText("slug"),
        )
        val modelId = this.modelService.insert(model)

        var imageCounter = 1L
        var counters = FileCounters()

        metadata.get("tags").forEach { tag ->
            val modelTag = ModelTag(userId, modelId, tag.asText())
            this.modelTagsService.insert(modelTag)
        }

        metadata.get("designExtension").get("design_pictures").forEach { image ->
            storeFile(
                image.getText("url"),
                userId,
                modelId,
                ModelFileType.image,
                image.getText("name"),
                imageCounter++,
            )
        }

        // If "Download all" zip is available download it
        counters = downloadAndStore(downloadUrl, userId, modelId, counters)

        // Download files and information for all profiles (called instances in the API)
        val instanceDescriptionExtension = mutableListOf<String>()
        metadata.get("instances").forEach { instance ->
            // Yes, "extention" is written incorrectly in MakerWorlds API
            val modelInfo = instance.get("extention").get("modelInfo")

            val instanceId = instance.getLong("id")
            val instanceName = instance.getText("title")
            val instanceDescription = instance.getText("summary")
            val instanceUpdateTime = instance.getText("updateTime")
            val instanceCoverImageName = instance.getText("cover").getFilenameWithExtensionFromUrl()
            val instanceAuxImageNames = mutableListOf<String>()
            val instanceAuthor = instance.get("instanceCreator")!!.getText("name")
            val instanceNeedsAMS = instance.getBoolean("needAms")
            val instanceFilaments = instance.get("instanceFilaments")?.map { filament -> filamentToString(filament) }
            val instanceDeviceCompatibility = mutableListOf<String>()
            val instancePlates = modelInfo.get("plates")?.map { plate -> plateToString(instanceId, plate) }

            // Store cover image
            storeFile(
                instance.getText("cover"),
                userId,
                modelId,
                ModelFileType.image,
                instanceCoverImageName,
                imageCounter++,
            )

            // Store further images
            instance.get("pictures").forEach { image ->
                if (instanceCoverImageName != image.getText("url").getFilenameWithExtensionFromUrl()) {
                    instanceAuxImageNames.add(image.getText("url").getFilenameWithExtensionFromUrl())
                    storeFile(
                        image.getText("url"),
                        userId,
                        modelId,
                        ModelFileType.image,
                        image.getText("url").getFilenameWithExtensionFromUrl(),
                        imageCounter++,
                    )
                }
            }

            // Store plate images
            modelInfo.get("plates").forEach { plate ->
                storeFile(
                    plate.get("thumbnail").getText("url"),
                    userId,
                    modelId,
                    ModelFileType.image,
                    plateImageName(instanceId, plate),
                    imageCounter++,
                )
            }

            // Get device compatibilities
            instanceDeviceCompatibility.add(deviceCompatibilityToString(modelInfo.get("compatibility")))
            instanceDeviceCompatibility.addAll(
                modelInfo.get("otherCompatibility").map { co -> deviceCompatibilityToString(co) },
            )

            instanceDescriptionExtension.add(
                """## Profile $instanceId: *$instanceName*
                    |- **Author**: $instanceAuthor
                    |- **Last Update**: $instanceUpdateTime
                    |- **Cover Image**: $instanceCoverImageName
                    |- **Attached Images**: ${instanceAuxImageNames.joinToString(", ")}
                    |- **Needs AMS**: ${if (instanceNeedsAMS) "Yes" else "No"}
                    |- **Device Compatibilities**: ${instanceDeviceCompatibility.joinToString(", ")}
                    |${printFilaments(instanceFilaments ?: listOf())}
                    |
                    |### Description
                    |$instanceDescription
                    |
                    |### Plates
                    |${instancePlates?.joinToString("\n\n") { it }}
                """.trimMargin(),
            )

            // Download all available instance files in 3mf and stl
            instanceFileTypes.forEach { fileType ->
                val instanceDownloadUrl = instanceUrl.format(instanceId) + fileType
                counters = downloadAndStore(instanceDownloadUrl, userId, modelId, counters, "profile${instanceId}_")
            }
        }

        modelService.appendDescription(userId, modelId, instanceDescriptionExtension.joinToString("\n\n"))

        // Store json result from API
        storeData(metadata, userId, modelId, ModelFileType.various, "api-response.json", counters.various++)

        return modelId
    }

    private fun downloadAndStore(
        downloadUrl: String,
        userId: Long,
        modelId: Long,
        counters: FileCounters,
        instanceFilenamePrefix: String = "",
    ): FileCounters {
        Fuel.get(downloadUrl).ua()
            .header("Host", "makerworld.com")
            .header(
                "Cookie",
                "token=${userSettingsService.getSetting(userId, UserSettingKey.MakerWorldSessionToken)!!.value}",
            ).responseString { request, response, result ->
                when (result) {
                    is Result.Success<*> -> {
                        val download = JacksonModule.mapper.readValue<JsonNode>(result.get())
                        val fileFullName = download.getText("url").getFilenameWithExtensionFromUrl()
                        val fileType =
                            if (FileType.getFileExtension(fileFullName) == "zip") {
                                ModelFileType.various
                            } else {
                                ModelFileType.model
                            }

                        storeFile(
                            download.getText("url"),
                            userId,
                            modelId,
                            fileType,
                            instanceFilenamePrefix + fileFullName,
                            if (fileType == ModelFileType.various) counters.various++ else counters.model++,
                        )
                        // TODO
                        // The current solution is to just store the zip
                        // In future it could be automatically unzipped, but folder levels should be taken into account
                        // as duplicate files might change and folder names might contain relevant information
                        //   unzipDownload(zipDownloadUrl, userId, modelId)
                    }

                    is Result.Failure<*> -> {
                        logger.error(
                            "MakerWorld download failed: {} {} - URL: {}",
                            response.statusCode,
                            response.responseMessage,
                            request.url,
                        )
                        logger.debug("Request details - Headers: {}, Params: {}", request.headers, request.parameters)
                        logger.debug("Exception: ", result.getException())
                    }
                }
            }

        return counters
    }

    private fun filamentToString(filament: JsonNode): String = filament.getText("type") + ", " +
        filament.getText("color") + ", " +
        filament.getText("usedM") + "m, " +
        filament.getText("usedG") + "g"

    private fun deviceCompatibilityToString(devComp: JsonNode): String = devComp.getText(
        "devProductName",
    ) + " (Nozzle: " + devComp.getText("nozzleDiameter") + ")"

    private fun plateToString(instanceId: Long, plate: JsonNode): String {
        val plateIdx = plate.getLong("index")
        val plateName = plate.getText("name")
        val plateThumbnail = plateImageName(instanceId, plate)
        val plateFilaments = plate.get("filaments").map { filament -> filamentToString(filament) }

        return """#### Plate (No. $plateIdx) $plateName
            |- **Thumbnail**: $plateThumbnail
            |${printFilaments(plateFilaments)}
            |
        """.trimMargin()
    }

    private fun plateImageName(instanceId: Long, plate: JsonNode): String =
        "profile" + instanceId + "-" +
            "plate" + plate.getText("index") + "-" +
            plate.get("thumbnail").getText("name")

    private fun printFilaments(filaments: List<String>): String {
        return if (filaments.isNotEmpty()) {
            return "- **Filaments**:\n" +
                filaments.joinToString("\n") { "  - $it" }
        } else {
            ""
        }
    }
}
