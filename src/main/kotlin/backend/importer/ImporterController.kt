package backend.importer

import com.google.inject.Inject
import core.javalin.userId
import data.dto.MessageCode
import data.dto.ServerMessage
import data.importer.BaseImporter
import data.importer.ImportSource
import data.importer.exception.ImportFailedException
import data.importer.exception.MissingCredentialsException
import data.services.ModelService
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import io.javalin.http.bodyAsClass
import io.javalin.http.pathParamAsClass

class ImporterController @Inject constructor(
    private val modelService: ModelService,
) {
    fun importModel(ctx: Context) {
        val chosenImporter = ctx.pathParamAsClass<ImportSource>("importer").get()
        val importer = BaseImporter.getImporter(chosenImporter)

        try {
            val params = ctx.bodyAsClass<Map<String, String>>()
            val modelId = importer.import(ctx.userId(), params)
            ctx.json(modelService.get(ctx.userId(), modelId) ?: throw NotFoundResponse())
        } catch (e: MissingCredentialsException) {
            ServerMessage(MessageCode.MissingCredentials).send(ctx)
        } catch (e: ImportFailedException) {
            ServerMessage(MessageCode.OrderFailed).send(ctx)
        }
    }

    fun getEnabled(ctx: Context) {
        ctx.json(BaseImporter.getEnabledImporters())
    }
}
