package backend.collections

import com.google.inject.Inject
import core.javalin.modelId
import core.javalin.userId
import data.bean.Collection
import data.services.AccessService
import data.services.CollectionService
import data.services.ModelService
import io.javalin.http.Context
import io.javalin.http.ForbiddenResponse
import io.javalin.http.NotFoundResponse
import io.javalin.http.bodyAsClass
import io.javalin.http.pathParamAsClass

class CollectionController @Inject constructor(
    private val collectionService: CollectionService,
    private val modelService: ModelService,
    private val accessService: AccessService,
) {
    fun getCollections(ctx: Context) {
        ctx.json(collectionService.getCollections(ctx.userId()))
    }

    fun getCollection(ctx: Context) {
        val id = ctx.pathParamAsClass<Long>("id").get()

        val collection = collectionService.get(id) ?: throw NotFoundResponse()

        if (collection.userId == ctx.userId()) {
            ctx.json(collection)
        } else {
            throw ForbiddenResponse()
        }
    }

    fun createCollection(ctx: Context) {
        val collection = ctx.bodyAsClass<Collection>().copy(userId = ctx.userId())

        if (collection.mainModel != null && !accessService.userOwnsModel(ctx.userId(), collection.mainModel)) {
            throw ForbiddenResponse()
        }

        val id = collectionService.insert(collection)
        ctx.json(collectionService.get(id)!!)
    }

    fun updateCollection(ctx: Context) {
        val id = ctx.pathParamAsClass<Long>("id").get()
        val collection = ctx.bodyAsClass<Collection>().copy(userId = ctx.userId())
        val collectionFromDb = collectionService.get(id) ?: throw NotFoundResponse()

        if (
            id == collection.id && collection.userId == collectionFromDb.userId &&
            (collection.mainModel == null || accessService.userOwnsModel(ctx.userId(), collection.mainModel))
        ) {
            collectionService.update(collection)
            ctx.json(collectionService.get(id)!!)
        } else {
            throw ForbiddenResponse()
        }
    }

    fun deleteCollection(ctx: Context) {
        val id = ctx.pathParamAsClass<Long>("id").get()
        val collectionFromDb = collectionService.get(id) ?: throw NotFoundResponse()

        if (ctx.userId() == collectionFromDb.userId) {
            collectionService.delete(id)
            ctx.json(collectionFromDb)
        } else {
            throw ForbiddenResponse()
        }
    }

    fun createRelation(ctx: Context) {
        val (id, modelId, access) = getAndCheckRelations(ctx)

        if (access) {
            collectionService.insertRelation(id, modelId)
        } else {
            throw ForbiddenResponse()
        }
    }

    fun deleteRelation(ctx: Context) {
        val (id, modelId, access) = getAndCheckRelations(ctx)

        if (access) {
            collectionService.deleteRelation(id, modelId)
        } else {
            throw ForbiddenResponse()
        }
    }

    fun getCollectionsOfModel(ctx: Context) {
        val modelFromDb = modelService.get(ctx.modelId()) ?: throw NotFoundResponse()

        if (modelFromDb.userId == ctx.userId()) {
            ctx.json(collectionService.getCollectionsOfModel(ctx.modelId()))
        } else {
            throw ForbiddenResponse()
        }
    }

    fun getModelsInCollection(ctx: Context) {
        val id = ctx.pathParamAsClass<Long>("id").get()

        val collectionFromDb = collectionService.get(id) ?: throw NotFoundResponse()

        if (collectionFromDb.userId == ctx.userId()) {
            ctx.json(collectionService.getModelsInCollection(id))
        } else {
            throw ForbiddenResponse()
        }
    }

    private fun getAndCheckRelations(ctx: Context): Triple<Long, Long, Boolean> {
        val id = ctx.pathParamAsClass<Long>("id").get()
        val modelId = ctx.pathParamAsClass<Long>("modelId").get()

        val collectionFromDb = collectionService.get(id) ?: throw NotFoundResponse()
        val modelFromDb = modelService.get(modelId) ?: throw NotFoundResponse()
        val access = collectionFromDb.userId == ctx.userId() && modelFromDb.userId == ctx.userId()

        return Triple(id, modelId, access)
    }
}
