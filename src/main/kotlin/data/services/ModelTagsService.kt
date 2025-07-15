package data.services

import com.google.inject.Inject
import data.bean.ModelTag
import data.dao.ModelTagsDao
import data.dto.ModelTagCount

class ModelTagsService @Inject constructor(
    private val modelTagsDao: ModelTagsDao,
) {
    fun insert(modelTag: ModelTag) {
        try {
            return modelTagsDao.insert(modelTag)
        } catch (e: Exception) {
            // do nothing
        }
    }

    fun getWithCount(userId: Long): List<ModelTagCount> = modelTagsDao.getTagWithCount(userId)

    fun get(userId: Long, modelId: Long): List<ModelTag> = modelTagsDao.get(userId, modelId)

    fun get(userId: Long, modelId: Long, tag: String): ModelTag? = modelTagsDao.get(userId, modelId, tag)

    fun delete(modelTag: ModelTag) {
        modelTagsDao.delete(modelTag)
    }
}
