package storage.bean

import core.config.bean.storage.AppConfigLocalStorage
import java.io.File
import java.io.InputStream
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

class LocalStorage(
    override val storageConfig: AppConfigLocalStorage,
) : AbstractStorage(storageConfig) {
    init {
        baseDir = storageConfig.url
    }

    override fun mkDirs(filepath: String) {
        if (!Path(filepath).exists()) {
            Path(filepath).createDirectories()
        }
    }

    override fun uploadFile(file: InputStream, targetPath: String, filename: String) {
        mkDirs(targetPath)

        val diskFile = File(targetPath + filename)

        file.use { input ->
            diskFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        file.close()
    }

    override fun getFile(filepath: String): InputStream {
        return File(filepath).inputStream()
    }

    override fun moveFile(sourceFilePath: String, targetPath: String, targetFileName: String) {
        mkDirs(targetPath)
        File(sourceFilePath).renameTo(File(targetPath + targetFileName))
    }

    override fun deleteFile(filepath: String) {
        File(filepath).delete()
    }
}
