package data.importer

import com.fasterxml.jackson.databind.JsonNode
import com.google.inject.Guice
import com.google.inject.Injector
import core.BackendModule
import core.config.AppConfig
import core.httpclient.HttpClient
import data.bean.ModelFile
import data.bean.ModelFileType
import data.services.ModelFileService
import data.services.ModelService
import data.services.ModelTagsService
import data.services.UserSettingsService
import dev.misfitlabs.kotlinguice4.getInstance
import io.github.furstenheim.CopyDown
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.runBlocking
import net.lingala.zip4j.io.inputstream.ZipInputStream
import org.slf4j.Logger
import storage.Storage
import java.io.ByteArrayInputStream

abstract class BaseImporter {
    protected val modelService = injector.getInstance<ModelService>()
    protected val modelTagsService = injector.getInstance<ModelTagsService>()
    protected val userSettingsService = injector.getInstance<UserSettingsService>()
    protected val converter: CopyDown = CopyDown()

    companion object {
        private val injector: Injector = Guice.createInjector(BackendModule())
        val config = injector.getInstance<AppConfig>()
        private val modelFileService = injector.getInstance<ModelFileService>()
        val logger: Logger = injector.getInstance()

        const val USER_AGENT = HttpClient.USER_AGENT

        fun isEnabled(importer: ImportSource): Boolean = getEnabledImporters().contains(importer)

        fun getEnabledImporters(): List<ImportSource> {
            val importers = config.config.importer

            val returnValue = mutableListOf<ImportSource>()

            if (importers.cults3d != null) returnValue.add(ImportSource.Cults3D)
            if (importers.instructables) returnValue.add(ImportSource.Instructables)
            if (importers.makerworld) returnValue.add(ImportSource.MakerWorld)
            if (importers.myminifactory != null) returnValue.add(ImportSource.MyMiniFactory)
            if (importers.printables) returnValue.add(ImportSource.Printables)
            if (importers.sketchfab != null) returnValue.add(ImportSource.Sketchfab)
            if (importers.thingiverse != null) returnValue.add(ImportSource.Thingiverse)

            return returnValue
        }

        fun getImporter(importer: ImportSource): BaseImporter = when (importer) {
            ImportSource.Cults3D -> Cults3DImporter()
            ImportSource.Instructables -> InstructablesImporter()
            ImportSource.MakerWorld -> MakerWorldImporter()
            ImportSource.MyMiniFactory -> MyMiniFactoryImporter()
            ImportSource.Printables -> PrintablesImporter()
            ImportSource.Sketchfab -> SketchfabImporter()
            ImportSource.Thingiverse -> ThingiverseImporter()
        }

        fun storeFile(
            downloadUrl: String,
            userId: Long,
            modelId: Long,
            type: ModelFileType,
            filename: String,
            position: Long,
        ) = runBlocking {
            val body = HttpClient.instance.get(downloadUrl).readRawBytes()
            val size = body.size.toLong()

            val storage = Storage.getRandomStorageClass(size)
            val targetFilePath = storage.getUserFileTypePath(userId, modelId, type)
            storage.uploadFile(body.inputStream(), targetFilePath, filename)
            modelFileService.insertModelFile(
                ModelFile(-1, storage.storageConfig.name, userId, modelId, type, filename, position, size),
            )
        }

        fun storeData(
            data: JsonNode,
            userId: Long,
            modelId: Long,
            type: ModelFileType,
            filename: String,
            position: Long,
        ) {
            val data = ByteArrayInputStream(data.toPrettyString().toByteArray())
            val size = data.available().toLong()

            val storage = Storage.getRandomStorageClass(size)
            val targetFilePath = storage.getUserFileTypePath(userId, modelId, type)
            storage.uploadFile(data, targetFilePath, filename)
            modelFileService.insertModelFile(
                ModelFile(-1, storage.storageConfig.name, userId, modelId, type, filename, position, size),
            )
        }

        // Must verify before that it is a zip file!
        fun unzipDownload(downloadUrl: String, userId: Long, modelId: Long) = runBlocking {
            val body = HttpClient.instance.get(downloadUrl).readRawBytes()

            val zipIS = ZipInputStream(body.inputStream())
            var file = zipIS.nextEntry
            while (file != null) {
                val filename = file.fileName
                logger.debug("Unzipping file: {} ({})", filename, file)

                file = zipIS.nextEntry
            }
        }
    }

    abstract fun import(userId: Long, args: Map<String, String>): Long
}
