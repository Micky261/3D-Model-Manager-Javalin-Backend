package data.services

import com.google.inject.Inject
import data.bean.ModelLink
import data.dao.ModelLinkDao
import io.javalin.http.NotFoundResponse

class ModelLinkService @Inject constructor(
    private val modelLinkDao: ModelLinkDao,
) {
    fun get(id: Long): ModelLink {
        return modelLinkDao.get(id) ?: throw NotFoundResponse()
    }

    fun get(userId: Long, modelId: Long): List<ModelLink> {
        return modelLinkDao.getAllByModel(modelId, userId)
    }

    fun insert(modelLink: ModelLink): ModelLink {
        return get(modelLinkDao.insert(modelLink))
    }

    fun update(modelLink: ModelLink) {
        modelLinkDao.update(modelLink)
    }

    fun delete(id: Long, userId: Long) {
        modelLinkDao.delete(id, userId)
    }
}
