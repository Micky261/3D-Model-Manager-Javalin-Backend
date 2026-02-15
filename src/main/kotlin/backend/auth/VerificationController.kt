package backend.auth

import com.google.inject.Inject
import core.email.EmailService
import core.javalin.userId
import data.dto.ServerMessage
import data.services.EmailVerificationService
import data.services.UserService
import data.services.VerifyResult
import io.javalin.http.Context

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

        when (emailVerificationService.verifyEmail(token)) {
            VerifyResult.SUCCESS ->
                ServerMessage("EMAIL_VERIFIED", "Your email has been verified successfully").send(ctx, 200)
            VerifyResult.INVALID_TOKEN ->
                ServerMessage("INVALID_OR_EXPIRED_TOKEN", "The verification link is invalid or has expired").send(ctx, 400)
            VerifyResult.EMAIL_ALREADY_TAKEN ->
                ServerMessage("EMAIL_ALREADY_TAKEN", "The email address is already taken by another account").send(ctx, 409)
        }
    }

    fun resend(ctx: Context) {
        val userId = ctx.userId()
        val user = userService.getById(userId)

        if (user == null) {
            ServerMessage("USER_NOT_FOUND", "User not found").send(ctx, 404)
            return
        }

        val baseUrl = ctx.header("Origin") ?: ctx.header("Referer")?.substringBefore("/email-resend") ?: ""

        if (user.emailVerifiedAt == null) {
            val token = emailVerificationService.createVerificationToken(userId)
            try {
                emailService.sendVerificationEmail(user.email, user.name, token, baseUrl)
                ServerMessage("VERIFICATION_RESENT", "Verification email has been resent").send(ctx, 200)
            } catch (e: Exception) {
                ServerMessage("EMAIL_SEND_FAILED", "Failed to send verification email").send(ctx, 500)
            }
            return
        }

        if (user.pendingEmail != null) {
            val token = emailVerificationService.createVerificationToken(userId)
            try {
                emailService.sendEmailChangeVerification(user.pendingEmail, user.name, token, baseUrl)
                ServerMessage("VERIFICATION_RESENT", "Verification email has been resent").send(ctx, 200)
            } catch (e: Exception) {
                ServerMessage("EMAIL_SEND_FAILED", "Failed to send verification email").send(ctx, 500)
            }
            return
        }

        ServerMessage("ALREADY_VERIFIED", "Your email is already verified").send(ctx, 400)
    }
}
