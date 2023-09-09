package data

import data.dao.ModelDao
import data.dao.ModelDaoClass
import data.dao.ModelFileDao
import data.dao.ModelLinkDao
import data.dao.ModelTagsDao
import data.dao.SessionsDao
import data.dao.UserDao
import dev.misfitlabs.kotlinguice4.KotlinModule

object DaoModule : KotlinModule() {
    override fun configure() {
        bind<ModelDao>().toProvider<ModelDaoProvider>()
        bind<ModelDaoClass>()
        bind<ModelFileDao>().toProvider<ModelFileDaoProvider>()
        bind<ModelLinkDao>().toProvider<ModelLinkDaoProvider>()
        bind<ModelTagsDao>().toProvider<ModelTagsDaoProvider>()
        bind<SessionsDao>().toProvider<SessionsDaoProvider>()
        bind<UserDao>().toProvider<UserDaoProvider>()
    }
}
