package backend.auth

import com.google.inject.Inject
import core.config.AppConfig
import core.config.bean.RegistrationMode
import core.email.EmailService
import core.javalin.locale
import data.dto.Login
import data.dto.MessageCode
import data.dto.Register
import data.dto.ServerMessage
import data.dto.requests.PasswordResetRequest
import data.dto.requests.ResetPasswordRequest
import data.services.EmailVerificationService
import data.services.InvitationTokenService
import data.services.PasswordResetService
import data.services.SessionsService
import data.services.UserService
import io.javalin.http.Context
import io.javalin.http.bodyAsClass

class AuthController @Inject constructor(
    private val sessionsService: SessionsService,
    private val userService: UserService,
    private val emailVerificationService: EmailVerificationService,
    private val emailService: EmailService,
    private val appConfig: AppConfig,
    private val invitationTokenService: InvitationTokenService,
    private val passwordResetService: PasswordResetService,
) {
    fun login(ctx: Context) {
        val body = ctx.bodyAsClass<Login>()

        val user = userService.checkLogin(body.email, body.password)
        if (user != null) {
            if (user.emailVerifiedAt == null) {
                ServerMessage(MessageCode.EmailNotVerified).send(ctx)
                return
            }
            ctx.json(sessionsService.createSession(user))
        } else {
            ServerMessage(MessageCode.UserDataIncorrect).send(ctx)
        }
    }

    fun getRegistrationInfo(ctx: Context) {
        ctx.json(mapOf("registrationMode" to appConfig.config.general.registrationMode.value))
    }

    fun register(ctx: Context) {
        val body = ctx.bodyAsClass<Register>()
        val registrationMode = appConfig.config.general.registrationMode
        val isFirstUser = !userService.hasAnyUsers()

        if (registrationMode == RegistrationMode.Disabled && !isFirstUser) {
            ServerMessage(MessageCode.RegistrationDisabled).send(ctx)
            return
        }

        if (registrationMode == RegistrationMode.Token && !isFirstUser) {
            if (body.invitationToken.isNullOrBlank()) {
                ServerMessage(MessageCode.InvitationTokenRequired).send(ctx)
                return
            }
            if (invitationTokenService.validateToken(body.invitationToken) == null) {
                ServerMessage(MessageCode.InvalidInvitationToken).send(ctx)
                return
            }
        }

        if (body.name.isBlank() || body.email.isBlank() || body.password.isBlank()) {
            ServerMessage(MessageCode.InvalidInput).send(ctx)
            return
        }

        if (userService.emailExists(body.email)) {
            ServerMessage(MessageCode.EmailAlreadyExists).send(ctx)
            return
        }

        val userId = userService.insert(body.name, body.email, body.password)

        if (isFirstUser) {
            userService.makeAdmin(userId)
        }

        if (registrationMode == RegistrationMode.Token && !body.invitationToken.isNullOrBlank() && !isFirstUser) {
            invitationTokenService.markUsed(body.invitationToken, userId)
        }

        val token = emailVerificationService.createVerificationToken(userId)

        val baseUrl = ctx.header("Origin") ?: ctx.header("Referer")?.substringBefore("/auth") ?: ""

        try {
            emailService.sendVerificationEmail(body.email, body.name, token, baseUrl, ctx.locale())
            ServerMessage(MessageCode.RegistrationSuccess).send(ctx)
        } catch (e: Exception) {
            ServerMessage(MessageCode.EmailSendFailed).send(ctx, 201)
        }
    }

    fun requestPasswordReset(ctx: Context) {
        val body = ctx.bodyAsClass<PasswordResetRequest>()
        val user = userService.get(body.email)

        if (user != null && user.emailVerifiedAt != null) {
            val token = passwordResetService.createResetToken(body.email)
            val baseUrl = ctx.header("Origin") ?: ctx.header("Referer")?.substringBefore("/auth") ?: ""

            try {
                emailService.sendPasswordResetEmail(body.email, token, baseUrl, ctx.locale())
            } catch (_: Exception) {
                // Silently ignore send failures to not leak information
            }
        }

        // Always return 200 to prevent email enumeration
        ServerMessage(MessageCode.PasswordResetRequested).send(ctx)
    }

    fun resetPassword(ctx: Context) {
        val body = ctx.bodyAsClass<ResetPasswordRequest>()
        val reset = passwordResetService.getByToken(body.token)

        if (reset == null || !passwordResetService.isTokenValid(reset)) {
            ServerMessage(MessageCode.InvalidOrExpiredToken).send(ctx)
            return
        }

        val user = userService.get(reset.email)
        if (user == null) {
            ServerMessage(MessageCode.InvalidOrExpiredToken).send(ctx)
            return
        }

        val minPasswordLength = appConfig.config.general.minPasswordLength
        if (body.password.length < minPasswordLength) {
            ServerMessage(MessageCode.PasswordTooShort).send(ctx)
            return
        }

        userService.changePassword(user.id, body.password)
        passwordResetService.deleteByToken(body.token)

        ServerMessage(MessageCode.PasswordResetSuccess).send(ctx)
    }
}
