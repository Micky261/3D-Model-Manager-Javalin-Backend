package backend.auth

import com.google.inject.Inject
import core.config.AppConfig
import core.config.bean.RegistrationMode
import core.email.EmailService
import data.dto.LoginDto
import data.dto.RegisterDto
import data.dto.ServerMessage
import data.services.EmailVerificationService
import data.services.InvitationTokenService
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
) {
    fun login(ctx: Context) {
        val body = ctx.bodyAsClass<LoginDto>()

        val user = userService.checkLogin(body.email, body.password)
        if (user != null) {
            if (user.emailVerifiedAt == null) {
                ServerMessage("EMAIL_NOT_VERIFIED", "Please verify your email address first").send(ctx, 403)
                return
            }
            ctx.json(sessionsService.createSession(user))
        } else {
            ServerMessage("LOGIN_ERROR", "Error on Login").send(ctx, 405)
        }
    }

    fun getRegistrationInfo(ctx: Context) {
        ctx.json(mapOf("registrationMode" to appConfig.config.general.registrationMode.value))
    }

    fun register(ctx: Context) {
        val body = ctx.bodyAsClass<RegisterDto>()
        val registrationMode = appConfig.config.general.registrationMode
        val isFirstUser = !userService.hasAnyUsers()

        if (registrationMode == RegistrationMode.Disabled && !isFirstUser) {
            ServerMessage("REGISTRATION_DISABLED", "Registration is currently disabled").send(ctx, 403)
            return
        }

        if (registrationMode == RegistrationMode.Token && !isFirstUser) {
            if (body.invitationToken.isNullOrBlank()) {
                ServerMessage("INVITATION_TOKEN_REQUIRED", "An invitation token is required to register").send(ctx, 403)
                return
            }
            if (invitationTokenService.validateToken(body.invitationToken) == null) {
                ServerMessage(
                    "INVALID_INVITATION_TOKEN",
                    "The invitation token is invalid, expired, or has already been used",
                ).send(ctx, 403)
                return
            }
        }

        if (body.name.isBlank() || body.email.isBlank() || body.password.isBlank()) {
            ServerMessage("INVALID_INPUT", "Name, email and password are required").send(ctx, 400)
            return
        }

        if (userService.emailExists(body.email)) {
            ServerMessage("EMAIL_ALREADY_EXISTS", "An account with this email already exists").send(ctx, 409)
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

        val baseUrl = ctx.header("Origin") ?: ctx.header("Referer")?.substringBefore("/register") ?: ""

        try {
            emailService.sendVerificationEmail(body.email, body.name, token, baseUrl)
            ServerMessage(
                "REGISTRATION_SUCCESS",
                "Registration successful. Please check your email to verify your account.",
            ).send(ctx, 201)
        } catch (e: Exception) {
            ServerMessage(
                "EMAIL_SEND_FAILED",
                "Registration successful but failed to send verification email." +
                    "Please request a new verification email.",
            ).send(ctx, 201)
        }
    }
}
