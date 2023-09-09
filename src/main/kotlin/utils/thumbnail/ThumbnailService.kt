package utils.thumbnail

import com.google.inject.Inject
import data.bean.FileType
import data.bean.ModelFile
import data.services.ModelFileService
import storage.Storage
import utils.thumbnail.exception.ThumbnailSizeNotValid
import java.io.File
import java.io.InputStream

class ThumbnailService @Inject constructor(
    private val modelFileService: ModelFileService,
) {
    fun getThumbnail(
        userId: Long,
        modelId: Long,
        format: ThumbnailFormat,
        size: Int,
        modelFile: ModelFile,
    ): InputStream? {
        checkSizeValid(format, size)

        val thumb = File(getPathWithFilename(userId, modelId, modelFile.filename, format, size))

        if (!(thumb.exists() && thumb.isFile)) {
            val tg = ThumbnailGenerator(
                userId,
                modelId,
                modelFileService.getFile(userId, modelFile.id)?.file ?: return null,
                modelFile.filename,
            )

            when (format) {
                ThumbnailFormat.Quadratic -> tg.generateThumbnailQuadratic(size)
                ThumbnailFormat.Rectangular -> tg.generateThumbnailRectangular(size)
            }
        }

        return thumb.inputStream()
    }

    fun getDefaultThumbnail(
        format: ThumbnailFormat,
        size: Int,
    ): InputStream {
        checkSizeValid(format, size)

        val filename = getFilename("DefaultModelImage.jpg", format, size)
        return this::class.java.classLoader.getResourceAsStream("images/models/$filename")!!
    }

    companion object {
        fun deleteThumbnails(userId: Long, modelId: Long, filename: String) {
            val st = Storage.getDefaultStorage()
            ThumbnailFormat.entries.forEach { format ->
                format.availableSizes.forEach { size ->
                    st.deleteFile(getPathWithFilename(userId, modelId, filename, format, size))
                }
            }
        }

        fun getFilename(filename: String, format: ThumbnailFormat, size: Int): String {
            return FileType.getFilenameWithoutExtension(filename) +
                "${format.postFixFormat.format(size)}.${FileType.getFileExtension(filename)}"
        }

        fun getPathWithFilename(
            userId: Long,
            modelId: Long,
            filename: String,
            format: ThumbnailFormat,
            size: Int,
        ): String {
            return Storage.getDefaultStorage().getThumbnailPath(userId, modelId) + getFilename(filename, format, size)
        }

        private fun checkSizeValid(format: ThumbnailFormat, size: Int) {
            if (!format.availableSizes.contains(size)) throw ThumbnailSizeNotValid(format, size)
        }
    }
}
