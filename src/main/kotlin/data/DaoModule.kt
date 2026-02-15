package data

import data.dao.CollectionDao
import data.dao.EmailVerificationDao
import data.dao.InvitationTokenDao
import data.dao.ModelDao
import data.dao.ModelDaoClass
import data.dao.ModelFileDao
import data.dao.ModelLinkDao
import data.dao.ModelTagsDao
import data.dao.PasswordResetDao
import data.dao.SessionsDao
import data.dao.UserDao
import data.dao.UserDaoClass
import data.dao.UserSettingsDao
import dev.misfitlabs.kotlinguice4.KotlinModule

object DaoModule : KotlinModule() {
    override fun configure() {
        bind<CollectionDao>().toProvider<CollectionDaoProvider>()
        bind<EmailVerificationDao>().toProvider<EmailVerificationDaoProvider>()
        bind<InvitationTokenDao>().toProvider<InvitationTokenDaoProvider>()
        bind<ModelDao>().toProvider<ModelDaoProvider>()
        bind<PasswordResetDao>().toProvider<PasswordResetDaoProvider>()
        bind<ModelDaoClass>()
        bind<ModelFileDao>().toProvider<ModelFileDaoProvider>()
        bind<ModelLinkDao>().toProvider<ModelLinkDaoProvider>()
        bind<ModelTagsDao>().toProvider<ModelTagsDaoProvider>()
        bind<SessionsDao>().toProvider<SessionsDaoProvider>()
        bind<UserDao>().toProvider<UserDaoProvider>()
        bind<UserDaoClass>()
        bind<UserSettingsDao>().toProvider<UserSettingsDaoProvider>()
    }
}
