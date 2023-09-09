package backend.models

import com.google.inject.Inject
import core.javalin.modelId
import core.javalin.userId
import data.bean.Model
import data.services.ModelService
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import io.javalin.http.bodyAsClass

class ModelController @Inject constructor(
    private val modelService: ModelService,
) {
    fun getModel(ctx: Context) {
        ctx.json(modelService.get(ctx.userId(), ctx.modelId()) ?: throw NotFoundResponse())
    }

    fun createModel(ctx: Context) {
        val model = ctx.bodyAsClass<Model>().copy(userId = ctx.userId())
        val modelId = modelService.insert(model)

        ctx.json(modelService.get(ctx.userId(), modelId)!!)
    }

    fun updateModel(ctx: Context) {
        val model = ctx.bodyAsClass<Model>().copy(
            userId = ctx.userId(),
            id = ctx.modelId(),
        )

        modelService.update(model)
        ctx.json(modelService.get(ctx.userId(), ctx.modelId())!!)
    }

    fun deleteModel(ctx: Context) {
        val model = modelService.get(ctx.userId(), ctx.modelId()) ?: throw NotFoundResponse()
        modelService.delete(ctx.userId(), ctx.modelId())
        ctx.json(model)
    }
}
