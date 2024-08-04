package data.services

import com.google.inject.Inject
import data.bean.Collection
import data.bean.Model
import data.dao.CollectionDao
import data.dao.ModelDao

class CollectionService @Inject constructor(
    private val collectionDao: CollectionDao,
    private val modelDao: ModelDao,
) {
    fun getCollections(userId: Long): List<Collection> {
        return collectionDao.getCollections(userId)
    }

    fun get(id: Long): Collection? {
        return collectionDao.get(id)
    }

    fun insert(collection: Collection): Long {
        return collectionDao.insert(collection)
    }

    fun update(collection: Collection) {
        return collectionDao.update(collection)
    }

    fun delete(id: Long) {
        getModelIdsInCollection(id).forEach { modelId -> deleteRelation(id, modelId) }
        collectionDao.delete(id)
    }

    fun insertRelation(id: Long, modelId: Long) {
        collectionDao.insertRelation(id, modelId)
    }

    fun getCollectionIdsByModel(modelId: Long): List<Long> {
        return collectionDao.getCollectionsOfModel(modelId)
    }

    fun getCollectionsOfModel(modelId: Long): List<Collection> {
        return getCollectionIdsByModel(modelId).mapNotNull { collectionId -> get(collectionId) }
    }

    fun getModelIdsInCollection(id: Long): List<Long> {
        return collectionDao.getModelsInCollection(id)
    }

    fun getModelsInCollection(id: Long): List<Model> {
        return getModelIdsInCollection(id).mapNotNull { modelId -> modelDao.get(modelId) }
    }

    fun deleteRelation(id: Long, modelId: Long) {
        // Check if deleted relation is mainModel and in this case delete it from collection
        val collectionFromDb = collectionDao.get(id)
        if (collectionFromDb?.mainModel == modelId) {
            collectionDao.update(collectionFromDb.copy(mainModel = null))
        }

        collectionDao.deleteRelation(id, modelId)
    }
}
