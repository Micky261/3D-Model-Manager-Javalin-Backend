package data

import com.google.inject.Inject
import com.google.inject.Provider
import data.dao.ModelDao
import data.dao.ModelFileDao
import data.dao.ModelLinkDao
import data.dao.ModelTagsDao
import data.dao.SessionsDao
import data.dao.UserDao
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand

class ModelDaoProvider @Inject constructor(
    private val jdbi: Jdbi,
) : Provider<ModelDao> {
    override fun get(): ModelDao = jdbi.onDemand()
}

class ModelFileDaoProvider @Inject constructor(
    private val jdbi: Jdbi,
) : Provider<ModelFileDao> {
    override fun get(): ModelFileDao = jdbi.onDemand()
}

class ModelLinkDaoProvider @Inject constructor(
    private val jdbi: Jdbi,
) : Provider<ModelLinkDao> {
    override fun get(): ModelLinkDao = jdbi.onDemand()
}

class ModelTagsDaoProvider @Inject constructor(
    private val jdbi: Jdbi,
) : Provider<ModelTagsDao> {
    override fun get(): ModelTagsDao = jdbi.onDemand()
}

class SessionsDaoProvider @Inject constructor(
    private val jdbi: Jdbi,
) : Provider<SessionsDao> {
    override fun get(): SessionsDao = jdbi.onDemand()
}

class UserDaoProvider @Inject constructor(
    private val jdbi: Jdbi,
) : Provider<UserDao> {
    override fun get(): UserDao = jdbi.onDemand()
}
