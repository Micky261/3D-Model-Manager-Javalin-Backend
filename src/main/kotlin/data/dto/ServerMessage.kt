package data.dto

import io.javalin.http.Context

enum class MessageCode(val httpStatus: Int) {
    // Auth
    AuthError(401),
    EmailNotVerified(403),
    UserDataIncorrect(401),

    // Registration
    RegistrationDisabled(403),
    InvitationTokenRequired(403),
    InvalidInvitationToken(403),
    InvalidInput(400),
    EmailAlreadyExists(409),
    RegistrationSuccess(201),

    // Email Verification
    InvalidToken(400),
    InvalidOrExpiredToken(400),
    EmailVerified(200),
    EmailAlreadyTaken(409),
    AlreadyVerified(400),
    VerificationResent(200),

    // Password
    WrongPassword(403),
    PasswordTooShort(400),
    PasswordChanged(200),
    PasswordResetRequested(200),
    PasswordResetSuccess(200),

    // Profile
    UserNotFound(404),
    NameChanged(200),
    EmailChanged(200),

    // Email Sending
    EmailSendFailed(500),

    // Access
    Forbidden(403),

    // Files
    TargetAlreadyExists(409),

    // Import
    MissingCredentials(424),
    OrderFailed(500),
    ContactAdmin(500),

    // Search
    SearchErrorNoFields(400),

    // Admin
    TokenDeleted(200),
    TokenNotFound(404),
    UserDeleted(200),
    CannotDeleteSelf(400),
    AdminEmailChanged(200),
}

data class ServerMessage(val messageCode: MessageCode) {
    fun send(ctx: Context, httpStatus: Int = messageCode.httpStatus) {
        ctx.status(httpStatus)
        ctx.json(this)
    }
}
