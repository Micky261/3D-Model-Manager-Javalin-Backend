package backend.models

import com.google.inject.Inject
import core.javalin.userId
import data.services.ModelService
import io.javalin.http.Context
import io.javalin.http.pathParamAsClass

class ModelsController @Inject constructor(
    private val modelService: ModelService,
) {
    fun getAllModels(ctx: Context) {
        ctx.json(modelService.getAllByUser(ctx.userId()))
    }

    fun getAllModelsWithTags(ctx: Context) {
        ctx.json(modelService.getAllByUserFull(ctx.userId()))
    }

    fun getRandomModels(ctx: Context) {
        ctx.json(modelService.getRandom(ctx.userId(), ctx.pathParamAsClass<Int>("num").get()))
    }

    fun getNewestModels(ctx: Context) {
        ctx.json(modelService.getNewest(ctx.userId(), ctx.pathParamAsClass<Int>("num").get()))
    }
}
