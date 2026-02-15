package backend.profile

import com.google.inject.Inject
import core.config.AppConfig
import core.email.EmailService
import core.javalin.userId
import data.dto.ChangeEmailRequest
import data.dto.ChangeNameRequest
import data.dto.ChangePasswordRequest
import data.dto.MessageCode
import data.dto.ServerMessage
import data.dto.UserSettingDto
import data.dto.UserSettingsType
import data.services.EmailVerificationService
import data.services.UserService
import data.services.UserSettingsService
import io.javalin.http.Context
import io.javalin.http.bodyAsClass

class ProfileController @Inject constructor(
    private val userSettingsService: UserSettingsService,
    private val userService: UserService,
    private val emailVerificationService: EmailVerificationService,
    private val emailService: EmailService,
    private val appConfig: AppConfig,
) {
    fun getSettingsAccounts(ctx: Context) {
        ctx.json(userSettingsService.getSettings(UserSettingsType.Account, ctx.userId()))
    }

    fun setSettingsAccounts(ctx: Context) {
        val receivedSettings = ctx.bodyAsClass<List<UserSettingDto>>()
            .map { it.toUserSetting(ctx.userId()) }

        userSettingsService.saveSettings(receivedSettings)

        ctx.json(userSettingsService.getSettings(UserSettingsType.Account, ctx.userId()))
    }

    fun getProfile(ctx: Context) {
        val user = userService.getById(ctx.userId())
        if (user == null) {
            ServerMessage(MessageCode.UserNotFound).send(ctx)
            return
        }
        val profile = mutableMapOf<String, String>("name" to user.name, "email" to user.email)
        if (user.pendingEmail != null) {
            profile["pendingEmail"] = user.pendingEmail
        }
        ctx.json(profile)
    }

    fun changePassword(ctx: Context) {
        val body = ctx.bodyAsClass<ChangePasswordRequest>()

        if (!userService.verifyPasswordForUser(ctx.userId(), body.currentPassword)) {
            ServerMessage(MessageCode.WrongPassword).send(ctx)
            return
        }

        val minPasswordLength = appConfig.config.general.minPasswordLength
        if (body.newPassword.length < minPasswordLength) {
            ServerMessage(MessageCode.PasswordTooShort).send(ctx)
            return
        }

        userService.changePassword(ctx.userId(), body.newPassword)
        ServerMessage(MessageCode.PasswordChanged).send(ctx)
    }

    fun changeName(ctx: Context) {
        val body = ctx.bodyAsClass<ChangeNameRequest>()
        userService.changeName(ctx.userId(), body.name)
        ServerMessage(MessageCode.NameChanged).send(ctx)
    }

    fun changeEmail(ctx: Context) {
        val body = ctx.bodyAsClass<ChangeEmailRequest>()

        if (!userService.verifyPasswordForUser(ctx.userId(), body.currentPassword)) {
            ServerMessage(MessageCode.WrongPassword).send(ctx)
            return
        }

        if (userService.emailExists(body.email)) {
            ServerMessage(MessageCode.EmailAlreadyExists).send(ctx)
            return
        }

        userService.setPendingEmail(ctx.userId(), body.email)

        val token = emailVerificationService.createVerificationToken(ctx.userId())
        val baseUrl = ctx.header("Origin") ?: ctx.header("Referer")?.substringBefore("/profile") ?: ""

        try {
            val user = userService.getById(ctx.userId())
            emailService.sendEmailChangeVerification(body.email, user?.name ?: "", token, baseUrl)
        } catch (_: Exception) {
            // Email send failure is non-critical here
        }

        ServerMessage(MessageCode.EmailChanged).send(ctx)
    }
}
