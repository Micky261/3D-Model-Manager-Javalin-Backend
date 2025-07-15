package storage.bean

import core.config.bean.storage.AppConfigStorage
import data.bean.ModelFileType
import java.io.InputStream

abstract class AbstractStorage(
    open val storageConfig: AppConfigStorage,
) {
    protected lateinit var baseDir: String
    private val defaultBaseDir = "default/"

    abstract fun mkDirs(filepath: String)

    abstract fun uploadFile(file: InputStream, targetPath: String, filename: String)

    abstract fun getFile(filepath: String): InputStream

    abstract fun moveFile(sourceFilePath: String, targetPath: String, targetFileName: String)

    abstract fun deleteFile(filepath: String)

    fun getUserFilePath(userId: Long, modelId: Long, type: ModelFileType, filename: String): String =
        "${getUserFileTypePath(userId, modelId, type)}$filename"

    fun getUserFileTypePath(userId: Long, modelId: Long, type: ModelFileType): String =
        "${getUserFileBasePath(userId, modelId)}$type/"

    fun getThumbnailPath(userId: Long, modelId: Long): String = "${getDefaultFileBasePath(userId, modelId)}thumbnails/"

    fun getUserFileBasePath(userId: Long, modelId: Long): String = "$baseDir$userId/$modelId/"

    fun getDefaultFileBasePath(userId: Long, modelId: Long): String = "$baseDir$defaultBaseDir$userId/$modelId/"
}
