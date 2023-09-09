package backend.importer

import com.google.inject.Inject
import core.javalin.userId
import data.importer.BaseImporter
import data.importer.ImportSource
import data.services.ModelService
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import io.javalin.http.bodyAsClass
import io.javalin.http.pathParamAsClass

class ImporterController @Inject constructor(
    private val modelService: ModelService,
) {
    fun importModel(ctx: Context) {
        // TODO: Move to ImportService
        val chosenImporter = ctx.pathParamAsClass<ImportSource>("importer").get()
        val importer = BaseImporter.getImporter(chosenImporter)

        if (importer != null) {
            val params = ctx.bodyAsClass<Map<String, String>>()
            val modelId = importer.import(ctx.userId(), params)

            // TODO: Thumbnail generation

            ctx.json(modelService.get(ctx.userId(), modelId) ?: throw NotFoundResponse())
        }
    }

    fun getEnabled(ctx: Context) {
        // TODO: Move to ImportService
        ctx.json(BaseImporter.getEnabledImporters())
    }
}
