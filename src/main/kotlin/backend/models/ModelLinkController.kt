package backend.models

import com.google.inject.Inject
import core.javalin.modelId
import core.javalin.userId
import data.bean.ModelLink
import data.services.AccessService
import data.services.ModelLinkService
import io.javalin.http.Context
import io.javalin.http.ForbiddenResponse
import io.javalin.http.bodyAsClass
import io.javalin.http.pathParamAsClass

class ModelLinkController @Inject constructor(
    private val modelLinkService: ModelLinkService,
    private val accessService: AccessService,
) {
    fun getLinksForModel(ctx: Context) {
        ctx.json(modelLinkService.get(ctx.userId(), ctx.modelId()))
    }

    fun addLink(ctx: Context) {
        val modelLink = ctx.bodyAsClass<ModelLink>().copy(
            userId = ctx.userId(),
            modelId = ctx.modelId(),
        )

        ctx.json(modelLinkService.insert(modelLink))
    }

    fun updateLink(ctx: Context) {
        val id = ctx.pathParamAsClass<Long>("id").get()
        val modelLink = ctx.bodyAsClass<ModelLink>().copy(id = id)

        val linkFromDb = modelLinkService.get(id)
        if (linkFromDb.userId == ctx.userId() && accessService.userOwnsModel(ctx.userId(), linkFromDb.modelId)) {
            modelLinkService.update(modelLink)

            ctx.json(modelLinkService.get(id))
        } else {
            throw ForbiddenResponse()
        }
    }

    fun deleteLink(ctx: Context) {
        val id = ctx.pathParamAsClass<Long>("id").get()

        val linkFromDb = modelLinkService.get(id)
        if (linkFromDb.userId == ctx.userId() && accessService.userOwnsModel(ctx.userId(), linkFromDb.modelId)) {
            modelLinkService.delete(id, ctx.userId())
        } else {
            throw ForbiddenResponse()
        }
    }
}
