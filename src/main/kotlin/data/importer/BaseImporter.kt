package data.importer

import com.github.kittinunf.fuel.Fuel
import com.google.inject.Guice
import com.google.inject.Injector
import core.BackendModule
import core.config.AppConfig
import data.bean.ModelFile
import data.bean.ModelFileType
import data.services.ModelFileService
import data.services.ModelService
import data.services.ModelTagsService
import dev.misfitlabs.kotlinguice4.getInstance
import io.github.furstenheim.CopyDown
import storage.Storage

abstract class BaseImporter {
    protected val modelService = injector.getInstance<ModelService>()
    protected val modelTagsService = injector.getInstance<ModelTagsService>()
    protected val converter: CopyDown = CopyDown()

    companion object {
        private val injector: Injector = Guice.createInjector(BackendModule())
        val config = injector.getInstance<AppConfig>()
        private val modelFileService = injector.getInstance<ModelFileService>()

        fun isEnabled(importer: ImportSource): Boolean {
            return getEnabledImporters().contains(importer)
        }

        fun getEnabledImporters(): List<ImportSource> {
            val importers = config.config.importer

            val returnValue = mutableListOf<ImportSource>()

            if (importers.cults3d != null) returnValue.add(ImportSource.Cults3D)
            if (importers.instructables) returnValue.add(ImportSource.Instructables)
            if (importers.myminifactory != null) returnValue.add(ImportSource.MyMiniFactory)
            if (importers.printables) returnValue.add(ImportSource.Printables)
            if (importers.sketchfab != null) returnValue.add(ImportSource.Sketchfab)
            if (importers.thingiverse != null) returnValue.add(ImportSource.Thingiverse)

            return returnValue
        }

        fun getImporter(importer: ImportSource): BaseImporter? {
            return when (importer) {
                ImportSource.Cults3D -> Cults3DImporter()
                ImportSource.Instructables -> InstructablesImporter()
                ImportSource.MyMiniFactory -> MyMiniFactoryImporter()
                ImportSource.Printables -> PrintablesImporter()
                ImportSource.Sketchfab -> SketchfabImporter()
                ImportSource.Thingiverse -> ThingiverseImporter()
            }
        }

        fun storeFile(
            downloadUrl: String,
            userId: Long,
            modelId: Long,
            type: ModelFileType,
            filename: String,
            position: Long,
        ) {
            val body = Fuel.get(downloadUrl).response().second
            val size = body.data.size.toLong()
            // println("$downloadUrl ${body.contentLength} ${body.data.size} ${body.data[1]}")

            val storage = Storage.getRandomStorageClass(size)
            val targetFilePath = storage.getUserFileTypePath(userId, modelId, type)
            storage.uploadFile(body.data.inputStream(), targetFilePath, filename)
            modelFileService.insertModelFile(
                ModelFile(-1, storage.storageConfig.name, userId, modelId, type, filename, position, size),
            )
        }
    }

    abstract fun import(userId: Long, args: Map<String, String>): Long
}
