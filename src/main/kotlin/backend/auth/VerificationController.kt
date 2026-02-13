package backend.auth

import com.google.inject.Inject
import core.javalin.userId
import data.dto.ServerMessage
import data.services.EmailVerificationService
import data.services.UserService
import io.javalin.http.Context
import utils.EmailService

class VerificationController @Inject constructor(
    private val emailVerificationService: EmailVerificationService,
    private val userService: UserService,
    private val emailService: EmailService,
) {
    fun verify(ctx: Context) {
        val token = ctx.pathParam("token")

        if (token.isBlank()) {
            ServerMessage("INVALID_TOKEN", "Invalid verification token").send(ctx, 400)
            return
        }

        val success = emailVerificationService.verifyEmail(token)

        if (success) {
            ServerMessage("EMAIL_VERIFIED", "Your email has been verified successfully").send(ctx, 200)
        } else {
            ServerMessage("INVALID_OR_EXPIRED_TOKEN", "The verification link is invalid or has expired").send(ctx, 400)
        }
    }

    fun resend(ctx: Context) {
        val userId = ctx.userId()
        val user = userService.getById(userId)

        if (user == null) {
            ServerMessage("USER_NOT_FOUND", "User not found").send(ctx, 404)
            return
        }

        if (user.emailVerifiedAt != null) {
            ServerMessage("ALREADY_VERIFIED", "Your email is already verified").send(ctx, 400)
            return
        }

        val token = emailVerificationService.createVerificationToken(userId)
        val baseUrl = ctx.header("Origin") ?: ctx.header("Referer")?.substringBefore("/email-resend") ?: ""

        try {
            emailService.sendVerificationEmail(user.email, user.name, token, baseUrl)
            ServerMessage("VERIFICATION_RESENT", "Verification email has been resent").send(ctx, 200)
        } catch (e: Exception) {
            ServerMessage("EMAIL_SEND_FAILED", "Failed to send verification email").send(ctx, 500)
        }
    }
}
