package backend.auth

import com.google.inject.Inject
import core.email.EmailService
import core.javalin.locale
import core.javalin.userId
import data.dto.MessageCode
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
            ServerMessage(MessageCode.InvalidToken).send(ctx)
            return
        }

        when (emailVerificationService.verifyEmail(token)) {
            VerifyResult.SUCCESS ->
                ServerMessage(MessageCode.EmailVerified).send(ctx)

            VerifyResult.INVALID_TOKEN ->
                ServerMessage(MessageCode.InvalidOrExpiredToken).send(ctx)

            VerifyResult.EMAIL_ALREADY_TAKEN ->
                ServerMessage(MessageCode.EmailAlreadyTaken).send(ctx)
        }
    }

    fun resend(ctx: Context) {
        val userId = ctx.userId()
        val user = userService.getById(userId)

        if (user == null) {
            ServerMessage(MessageCode.UserNotFound).send(ctx)
            return
        }

        val baseUrl = ctx.header("Origin") ?: ctx.header("Referer")?.substringBefore("/email-resend") ?: ""

        if (user.emailVerifiedAt == null) {
            val token = emailVerificationService.createVerificationToken(userId)
            try {
                emailService.sendVerificationEmail(user.email, user.name, token, baseUrl, ctx.locale())
                ServerMessage(MessageCode.VerificationResent).send(ctx)
            } catch (e: Exception) {
                ServerMessage(MessageCode.EmailSendFailed).send(ctx)
            }
            return
        }

        if (user.pendingEmail != null) {
            val token = emailVerificationService.createVerificationToken(userId)
            try {
                emailService.sendEmailChangeVerification(user.pendingEmail, user.name, token, baseUrl, ctx.locale())
                ServerMessage(MessageCode.VerificationResent).send(ctx)
            } catch (e: Exception) {
                ServerMessage(MessageCode.EmailSendFailed).send(ctx)
            }
            return
        }

        ServerMessage(MessageCode.AlreadyVerified).send(ctx)
    }
}
