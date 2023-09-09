package data.services

import com.google.inject.Inject
import data.bean.FileType
import data.bean.ModelFile
import data.bean.ModelFileType
import data.dao.ModelFileDao
import data.dto.FileWithMimeType
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.apache.commons.lang3.RandomStringUtils
import storage.Storage
import storage.exception.TargetFileAlreadyExistsException
import utils.thumbnail.ThumbnailService
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.time.Instant

class ModelFileService @Inject constructor(
    private val modelFilesDao: ModelFileDao,
) {
    fun insertModelFile(modelFile: ModelFile): Long {
        return modelFilesDao.insert(modelFile)
    }

    fun getModelFiles(modelId: Long, userId: Long): List<ModelFile> {
        return modelFilesDao.get(modelId, userId)
    }

    fun getModelFiles(modelId: Long, userId: Long, modelFileIds: List<Long>): List<ModelFile> {
        return modelFilesDao.get(modelId, userId, modelFileIds)
    }

    fun getModelFile(userId: Long, fileId: Long): ModelFile? {
        return modelFilesDao.getFileByUser(userId, fileId)
    }

    fun getModelFiles(modelId: Long, userId: Long, type: ModelFileType): List<ModelFile> {
        return modelFilesDao.get(modelId, userId, type)
    }

    fun getFile(userId: Long, fileId: Long): FileWithMimeType? {
        return getFile(modelFilesDao.getFileByUser(userId, fileId) ?: return null)
    }

    fun getMainImageFile(modelId: Long, userId: Long): FileWithMimeType? {
        return getFile(getMainImage(modelId, userId) ?: return null)
    }

    fun getMainImage(modelId: Long, userId: Long): ModelFile? {
        return modelFilesDao.getMainImage(modelId, userId)
    }

    fun getDefaultImageFile(): InputStream {
        return this::class.java.classLoader.getResourceAsStream("images/models/DefaultModelImage.jpg")!!
    }

    fun updateModelFile(userId: Long, fileId: Long, position: Long, type: ModelFileType, filename: String) {
        val dbFile = modelFilesDao.getFileByUser(userId, fileId) ?: return

        var updated = false

        if (type != dbFile.type || filename != dbFile.filename) {
            if (exists(dbFile.userId, dbFile.modelId, type, filename)) {
                throw TargetFileAlreadyExistsException()
            } else {
                val storage = Storage.getStorageClassByName(dbFile.storage)
                val sourceFilepath = storage.getUserFilePath(
                    dbFile.userId,
                    dbFile.modelId,
                    dbFile.type,
                    dbFile.filename,
                )
                val targetPath = storage.getUserFileTypePath(dbFile.userId, dbFile.modelId, type)
                storage.moveFile(sourceFilepath, targetPath, filename)
            }

            updated = true
        }

        updated = updated || position != dbFile.position

        if (updated) {
            modelFilesDao.update(fileId, position, type, filename)
        }
    }

    fun deleteModelFile(userId: Long, modelFileId: Long) {
        val file = modelFilesDao.getFileByUser(userId, modelFileId) ?: return

        val storage = Storage.getStorageClassByName(file.storage)
        storage.deleteFile(storage.getUserFilePath(file.userId, file.modelId, file.type, file.filename))
        ThumbnailService.deleteThumbnails(file.userId, file.modelId, file.filename)

        modelFilesDao.delete(modelFileId)
    }

    fun getZipFile(modelId: Long, userId: Long, type: ModelFileType): InputStream? {
        val fileList =
            if (type == ModelFileType.all) {
                getModelFiles(modelId, userId)
            } else {
                getModelFiles(modelId, userId, type)
            }

        if (fileList.isEmpty()) return null

        File("./temp/").mkdirs()
        val zip = ZipFile("./temp/${RandomStringUtils.randomAlphanumeric(8)}-${Instant.now().epochSecond}.zip")
        val zipParameters = ZipParameters()

        fileList.forEach { file ->
            val storage = Storage.getStorageClassByName(file.storage)
            val filepath = storage.getUserFilePath(userId, modelId, file.type, file.filename)

            val stream = storage.getFile(filepath)
            zipParameters.fileNameInZip = "${file.type}/${file.filename}"
            zip.addStream(stream, zipParameters)
        }

        zip.close()
        return FileInputStream(zip.file)
    }

    private fun getFile(file: ModelFile): FileWithMimeType {
        val storage = Storage.getStorageClassByName(file.storage)
        return FileWithMimeType(
            storage.getFile(storage.getUserFilePath(file.userId, file.modelId, file.type, file.filename)),
            FileType.getMimeTypeFromFilename(file.filename),
        )
    }

    fun exists(userId: Long, modelId: Long, type: ModelFileType, filename: String): Boolean {
        return modelFilesDao.checkDuplicate(modelId, userId, type, filename) != 0L
    }
}
