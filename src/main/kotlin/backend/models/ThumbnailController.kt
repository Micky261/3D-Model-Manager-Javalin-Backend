package backend.models

import com.google.inject.Inject
import core.javalin.modelId
import core.javalin.userId
import data.bean.FileType
import data.services.ModelFileService
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import io.javalin.http.pathParamAsClass
import io.javalin.http.queryParamAsClass
import utils.thumbnail.ThumbnailFormat
import utils.thumbnail.ThumbnailService

class ThumbnailController @Inject constructor(
    private val modelFileService: ModelFileService,
    private val thumbnailService: ThumbnailService,
) {
    fun getThumbnail(ctx: Context) {
        val fileId = ctx.pathParamAsClass<Long>("fileId").get()
        val size = ctx.queryParamAsClass<Int>("size").get()
        val format = ctx.queryParamAsClass<ThumbnailFormat>("format").getOrDefault(ThumbnailFormat.Rectangular)

        val modelFile = modelFileService.getModelFile(ctx.userId(), fileId) ?: throw NotFoundResponse()
        val file = thumbnailService.getThumbnail(ctx.userId(), modelFile.modelId, format, size, modelFile)

        if (file != null) {
            ctx.contentType(FileType.getMimeTypeFromFilename(modelFile.filename))
            ctx.result(file)
        } else {
            ctx.contentType(FileType.getMimeType("jpg"))
            ctx.result(thumbnailService.getDefaultThumbnail(format, size))
        }
    }

    fun getMainThumbnail(ctx: Context) {
        val size = ctx.queryParamAsClass<Int>("size").get()
        val format = ctx.queryParamAsClass<ThumbnailFormat>("format").getOrDefault(ThumbnailFormat.Rectangular)

        val modelFile = modelFileService.getMainImage(ctx.modelId(), ctx.userId())
        if (modelFile != null) {
            val thumb = thumbnailService.getThumbnail(ctx.userId(), ctx.modelId(), format, size, modelFile)!!
            ctx.contentType(FileType.getMimeTypeFromFilename(modelFile.filename))
            ctx.result(thumb)
        } else {
            ctx.contentType(FileType.getMimeType("jpg"))
            ctx.result(thumbnailService.getDefaultThumbnail(format, size))
        }
    }

    fun getDefaultThumbnail(ctx: Context) {
        val size = ctx.queryParamAsClass<Int>("size").get()
        val format = ctx.queryParamAsClass<ThumbnailFormat>("format").getOrDefault(ThumbnailFormat.Rectangular)

        ctx.contentType(FileType.getMimeType("jpg"))
        ctx.result(thumbnailService.getDefaultThumbnail(format, size))
    }
}
