package backend.files

import com.google.inject.Inject
import core.javalin.modelId
import core.javalin.userId
import data.bean.FileType
import data.bean.ModelFile
import data.bean.ModelFileType
import data.dto.ServerMessage
import data.services.ModelFileService
import io.javalin.http.Context
import io.javalin.http.ForbiddenResponse
import io.javalin.http.NotFoundResponse
import io.javalin.http.bodyAsClass
import io.javalin.http.formParamAsClass
import io.javalin.http.pathParamAsClass
import storage.Storage
import utils.Chunking
import utils.fileResponse
import utils.thumbnail.ThumbnailGenerator
import java.io.File

class FilesController @Inject constructor(
    private val modelFileService: ModelFileService,
) {
    fun getFiles(ctx: Context) {
        ctx.json(modelFileService.getModelFiles(ctx.modelId(), ctx.userId()))
    }

    fun getFilesWithType(ctx: Context) {
        val type = ctx.pathParamAsClass<ModelFileType>("type").get()

        ctx.json(modelFileService.getModelFiles(ctx.modelId(), ctx.userId(), type))
    }

    fun updateFiles(ctx: Context) {
        val files = ctx.bodyAsClass<List<ModelFile>>()
        if (files.any { it.userId != ctx.userId() || it.modelId != ctx.modelId() }) throw ForbiddenResponse()

        val filesFromDb = modelFileService.getModelFiles(ctx.modelId(), ctx.userId(), files.map { it.id })
        // Check whether all given files belong to the user by checking if the db has the same number of entries when given all model file ids
        if (files.count() != filesFromDb.count()) throw ForbiddenResponse()

        files.forEach { modelFileService.updateModelFile(ctx.userId(), it.id, it.position, it.type, it.filename) }

        ctx.json(modelFileService.getModelFiles(ctx.modelId(), ctx.userId()))
    }

    fun downloadZipFile(ctx: Context) {
        val type = ctx.pathParamAsClass<ModelFileType>("type").get()

        ctx.fileResponse(
            modelFileService.getZipFile(ctx.modelId(), ctx.userId(), type) ?: throw NotFoundResponse(),
            "download.zip",
        )
    }

    fun getMainImage(ctx: Context) {
        val fileData = modelFileService.getMainImage(ctx.modelId(), ctx.userId())
        val file = modelFileService.getFile(ctx.modelId(), fileData?.id ?: throw NotFoundResponse())

        if (file != null) {
            ctx.fileResponse(file.file, fileData.filename)
        } else {
            ctx.fileResponse(modelFileService.getDefaultImageFile(), "default.jpg")
        }
    }

    fun getFile(ctx: Context) {
        val fileId = ctx.pathParamAsClass<Long>("fileId").get()

        val fileData = modelFileService.getModelFile(ctx.userId(), fileId) ?: throw NotFoundResponse()
        val file = modelFileService.getFile(ctx.userId(), fileId) ?: throw NotFoundResponse()

        ctx.fileResponse(file.file, fileData.filename)
    }

    fun saveFile(ctx: Context) {
        val userId = ctx.userId()
        val modelId = ctx.modelId()
        val filename = ctx.formParamAsClass<String>("filename").get()
        val rType = ctx.formParamAsClass<ModelFileType>("type").get()
        val forceOverwrite = ctx.formParamAsClass<Boolean>("force-overwrite").get()

        val type = if (rType == ModelFileType.automatic) {
            FileType.getModelFileTypeFromFilename(filename) ?: ModelFileType.various
        } else {
            rType
        }

        val modelExists = modelFileService.exists(userId, modelId, type, filename)

        if (modelExists && !forceOverwrite) {
            ServerMessage("TargetAlreadyExists", "File already exists.").send(ctx, 409)
        } else {
            val file = ctx.uploadedFile("file")
            val chunk = ctx.formParamAsClass<Int>("chunk").get()
            val totalChunks = ctx.formParamAsClass<Int>("totalChunks").get()
            val timestamp = ctx.formParamAsClass<Long>("timestamp").get()

            if (file != null) {
                val path = Storage.temporaryStorageBasePath + "/chunked/$userId/$timestamp/"

                Chunking.streamToFileChunk(file.content(), path, filename, chunk)

                if (Chunking.checkReady(totalChunks, path, filename)) {
                    val chunking = Chunking(totalChunks, path, filename)
                    val targetFile = chunking.putTogether()

                    if (modelExists) {
                        // Delete existing file
                        val existingFile = modelFileService.getModelFile(userId, modelId, type, filename)!!
                        val delStorage = Storage.getStorageClassByName(existingFile.storage)
                        delStorage.deleteFile(delStorage.getUserFilePath(userId, modelId, type, filename))

                        // Upload new file
                        val uploadStorage = Storage.getRandomStorageClass(targetFile.length())
                        uploadStorage.uploadFile(
                            targetFile.inputStream(),
                            uploadStorage.getUserFileTypePath(userId, modelId, type),
                            filename,
                        )

                        // Update db information
                        modelFileService.overwriteFileInformation(
                            existingFile.id,
                            uploadStorage.storageConfig.name,
                            targetFile.length(),
                        )
                    } else {
                        // Upload file
                        val storage = Storage.getRandomStorageClass(targetFile.length())
                        storage.uploadFile(
                            targetFile.inputStream(),
                            storage.getUserFileTypePath(userId, modelId, type),
                            filename,
                        )

                        // Calculate next position
                        val highestPosition = modelFileService.getMaxPosition(userId, modelId, type)

                        modelFileService.insertModelFile(
                            ModelFile(
                                -1,
                                storage.storageConfig.name,
                                userId,
                                modelId,
                                type,
                                filename,
                                highestPosition + 1,
                                targetFile.length(),
                            ),
                        )
                    }

                    if (FileType.getModelFileTypeFromFilename(filename) === ModelFileType.image) {
                        ThumbnailGenerator(userId, modelId, targetFile.inputStream(), filename).generateThumbnailSet()
                    }

                    System.gc()
                    File(path).deleteRecursively()
                }
            }
        }
    }

    fun deleteFile(ctx: Context) {
        val fileId = ctx.pathParamAsClass<Long>("fileId").get()
        val file = modelFileService.getModelFile(ctx.userId(), fileId) ?: throw NotFoundResponse()

        if (file.userId != ctx.userId()) throw ForbiddenResponse()

        modelFileService.deleteModelFile(ctx.userId(), fileId)
    }
}
