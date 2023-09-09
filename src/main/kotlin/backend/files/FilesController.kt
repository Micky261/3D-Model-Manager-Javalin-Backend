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

        ctx.contentType("application/zip")
        ctx.result(modelFileService.getZipFile(ctx.modelId(), ctx.userId(), type) ?: throw NotFoundResponse())
    }

    fun getMainImage(ctx: Context) {
        val file = modelFileService.getMainImageFile(ctx.modelId(), ctx.userId())

        if (file != null) {
            ctx.contentType(file.mimeType)
            ctx.result(file.file)
        } else {
            ctx.contentType(FileType.getMimeType("jpg"))
            ctx.result(modelFileService.getDefaultImageFile())
        }
    }

    fun getFile(ctx: Context) {
        val fileId = ctx.pathParamAsClass<Long>("fileId").get()

        val file = modelFileService.getFile(ctx.userId(), fileId) ?: throw NotFoundResponse()

        ctx.contentType(file.mimeType)
        ctx.result(file.file)
    }

    fun saveFile(ctx: Context) {
        val userId = ctx.userId()
        val modelId = ctx.modelId()
        val filename = ctx.formParamAsClass<String>("filename").get()
        val type = ctx.formParamAsClass<ModelFileType>("type").get()

        if (modelFileService.exists(userId, modelId, type, filename)) {
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

                    val storage = Storage.getRandomStorageClass(targetFile.length())
                    storage.uploadFile(
                        targetFile.inputStream(),
                        storage.getUserFileTypePath(userId, modelId, type),
                        filename,
                    )

                    modelFileService.insertModelFile(
                        ModelFile(
                            -1,
                            storage.storageConfig.name,
                            userId,
                            modelId,
                            type,
                            filename,
                            999,
                            targetFile.length(),
                        ),
                    )

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
