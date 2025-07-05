package backend.importer

import com.google.inject.Inject
import core.javalin.userId
import data.dto.ServerMessage
import data.importer.BaseImporter
import data.importer.ImportSource
import data.services.ModelService
import io.javalin.http.Context
import io.javalin.http.FailedDependencyResponse
import io.javalin.http.InternalServerErrorResponse
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
        } catch (e: FailedDependencyResponse) {
            // TODO: Translations in frontend need grammar check, Translation keys should match standard
            // TODO: Define own Exceptions as its used multiple times
            ServerMessage("MISSING_SESSION_ID", "Cults SessionId is not set").send(ctx, 424)
        } catch (e: InternalServerErrorResponse) {
            // TODO: Translations in frontend need grammar check, Translation keys should match standard
            // TODO: Define own Exceptions as its used multiple times
            ServerMessage("ORDER_FAILED", "Order request to Cults3d failed").send(ctx, 500)
//        } catch (e: Exception) {
//            // TODO: Replace generic error
//            ServerMessage("CONTACT_ADMIN", "Contact admin").send(ctx, 500)
        }
    }

    fun getEnabled(ctx: Context) {
        ctx.json(BaseImporter.getEnabledImporters())
    }
}
