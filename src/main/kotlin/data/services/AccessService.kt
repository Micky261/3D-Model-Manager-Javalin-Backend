package data.services

import com.google.inject.Inject
import data.dao.ModelDao

class AccessService @Inject constructor(
    private val modelDao: ModelDao,
) {
    fun userOwnsModel(userId: Long, modelId: Long): Boolean = modelDao.get(modelId, userId) !== null
}
