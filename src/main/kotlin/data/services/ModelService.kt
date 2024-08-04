package data.services

import com.google.inject.Inject
import data.bean.Model
import data.bean.ModelFull
import data.dao.ModelDao
import data.dao.ModelDaoClass

class ModelService @Inject constructor(
    private val modelDao: ModelDao,
    private val modelDaoClass: ModelDaoClass,
    private val collectionService: CollectionService,
    private val modelFileService: ModelFileService,
    private val modelLinkService: ModelLinkService,
    private val modelTagsService: ModelTagsService,
) {
    fun insert(model: Model): Long {
        return modelDao.insert(model)
    }

    fun getAllByUser(userId: Long): List<Model> {
        return modelDao.getAllByUser(userId)
    }

    fun getAllByUserFull(userId: Long): List<ModelFull> {
        return modelDao.getAllByUser(userId).map { model ->
            ModelFull.from(
                model,
                modelLinkService.get(userId, model.id),
                modelTagsService.get(userId, model.id).map { it.tag },
            )
        }
    }

    fun getNewest(userId: Long, count: Int): List<ModelFull> {
        return modelDao.getNewestByUser(userId, count).map { model ->
            ModelFull.from(
                model,
                modelLinkService.get(userId, model.id),
                modelTagsService.get(userId, model.id).map { it.tag },
            )
        }
    }

    fun getRandom(userId: Long, count: Int): List<ModelFull> {
        return modelDao.getRandomByUser(userId, count).map { model ->
            ModelFull.from(
                model,
                modelLinkService.get(userId, model.id),
                modelTagsService.get(userId, model.id).map { it.tag },
            )
        }
    }

    fun get(userId: Long, id: Long): ModelFull? {
        val model = modelDao.get(id, userId) ?: return null

        return ModelFull.from(
            model,
            modelLinkService.get(userId, model.id),
            modelTagsService.get(userId, model.id).map { it.tag },
        )
    }

    fun get(id: Long): Model? {
        return modelDao.get(id) ?: return null
    }

    fun update(updatedModel: Model): ModelFull {
        modelDao.update(updatedModel)

        val model = modelDao.get(updatedModel.id, updatedModel.userId)!!
        return ModelFull.from(
            model,
            modelLinkService.get(model.userId, model.id),
            modelTagsService.get(model.userId, model.id).map { it.tag },
        )
    }

    fun delete(userId: Long, id: Long) {
        modelFileService.getModelFiles(id, userId).forEach { modelFileService.deleteModelFile(userId, it.id) }
        modelLinkService.get(userId, id).forEach { modelLinkService.delete(it.id, userId) }
        modelTagsService.get(userId, id).forEach { modelTagsService.delete(it) }
        collectionService.getCollectionIdsByModel(id).forEach { collectionId ->
            collectionService.deleteRelation(collectionId, id)
        }
        modelDao.delete(id, userId)
    }

    fun search(userId: Long, searchTerm: String, searchFields: Set<String>): List<Model> {
        return modelDaoClass.search(userId, searchFields.intersect(Model.searchableFields), searchTerm)
    }
}
