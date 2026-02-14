package data

import core.email.EmailService
import data.services.AccessService
import data.services.CollectionService
import data.services.EmailVerificationService
import data.services.ImportService
import data.services.InvitationTokenService
import data.services.ModelFileService
import data.services.ModelLinkService
import data.services.ModelService
import data.services.ModelTagsService
import data.services.PasswordResetService
import data.services.SessionsService
import data.services.UserService
import data.services.UserSettingsService
import dev.misfitlabs.kotlinguice4.KotlinModule
import utils.thumbnail.ThumbnailService

object ServiceModule : KotlinModule() {
    override fun configure() {
        // Services directly related to DAO
        bind<CollectionService>()
        bind<EmailVerificationService>()
        bind<ImportService>()
        bind<InvitationTokenService>()
        bind<ModelService>()
        bind<PasswordResetService>()
        bind<ModelFileService>()
        bind<ModelLinkService>()
        bind<ModelTagsService>()
        bind<SessionsService>()
        bind<UserService>()
        bind<UserSettingsService>()

        // Other services
        bind<AccessService>()
        bind<EmailService>()
        bind<ThumbnailService>()
    }
}
