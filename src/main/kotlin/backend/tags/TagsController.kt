package backend.tags

import com.google.inject.Inject
import core.javalin.modelId
import core.javalin.userId
import data.bean.ModelTag
import data.services.ModelTagsService
import io.javalin.http.Context
import io.javalin.http.pathParamAsClass

class TagsController @Inject constructor(
    private val modelTagsService: ModelTagsService,
) {
    fun getAllTags(ctx: Context) {
        ctx.json(modelTagsService.getWithCount(ctx.userId()))
    }

    fun getModelTags(ctx: Context) {
        ctx.json(modelTagsService.get(ctx.userId(), ctx.modelId()))
    }

    fun setModelTag(ctx: Context) {
        val tag = ctx.pathParamAsClass<String>("tag").get()

        modelTagsService.insert(ModelTag(ctx.userId(), ctx.modelId(), tag))
        ctx.json(modelTagsService.get(ctx.userId(), ctx.modelId(), tag)!!)
    }

    fun removeModelTag(ctx: Context) {
        val tag = ctx.pathParamAsClass<String>("tag").get()

        modelTagsService.delete(ModelTag(ctx.userId(), ctx.modelId(), tag))
    }
}
