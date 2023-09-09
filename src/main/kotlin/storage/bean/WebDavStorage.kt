package storage.bean

import com.github.sardine.Sardine
import com.github.sardine.SardineFactory
import com.github.sardine.impl.SardineException
import core.config.bean.storage.AppConfigWebDavStorage
import storage.exception.StorageRequestException
import utils.toUrl
import java.io.InputStream

class WebDavStorage(
    override val storageConfig: AppConfigWebDavStorage,
) : AbstractStorage(storageConfig) {
    private val webDavClient = SardineFactory.begin(storageConfig.username, storageConfig.password)

    init {
        baseDir = ""
    }

    override fun mkDirs(filepath: String) {
        try {
            webDavClient.mkDirs(storageConfig.url.toUrl(), filepath)
        } catch (e: SardineException) {
            throw StorageRequestException(e.statusCode).apply { initCause(e) }
        }
    }

    override fun uploadFile(file: InputStream, targetPath: String, filename: String) {
        try {
            mkDirs(targetPath)
            webDavClient.put((storageConfig.url + targetPath + filename).toUrl(), file.readAllBytes())
        } catch (e: SardineException) {
            throw StorageRequestException(e.statusCode).apply { initCause(e) }
        }
    }

    override fun getFile(filepath: String): InputStream {
        return try {
            webDavClient.get((storageConfig.url + filepath).toUrl())
        } catch (e: SardineException) {
            if (e.statusCode == 404) {
                InputStream.nullInputStream()
            } else {
                throw StorageRequestException(e.statusCode).apply { initCause(e) }
            }
        }
    }

    override fun moveFile(sourceFilePath: String, targetPath: String, targetFileName: String) {
        if (webDavClient.exists((storageConfig.url + sourceFilePath).toUrl())) {
            mkDirs(targetPath)

            webDavClient.move(
                (storageConfig.url + sourceFilePath).toUrl(),
                (storageConfig.url + targetPath + targetFileName).toUrl(),
            )
        }
    }

    override fun deleteFile(filepath: String) {
        if (webDavClient.exists((storageConfig.url + filepath).toUrl())) {
            webDavClient.delete((storageConfig.url + filepath).toUrl())
        }
    }

    /**
     * check if the given directories exist and create them if they do not
     */
    private fun Sardine.mkDirs(basePath: String, dirs: String) {
        if (exists("$basePath/$dirs")) {
            return
        }
        if (!exists(basePath)) {
            createDirectory(basePath)
        }
        dirs.split("/").fold("") { acc, folder ->
            val newAcc = "$acc/$folder"
            if (!exists("$basePath$newAcc")) {
                createDirectory("$basePath$newAcc")
            }

            newAcc
        }
    }
}
