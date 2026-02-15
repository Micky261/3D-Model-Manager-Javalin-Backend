package backend.admin

import com.google.inject.Inject
import data.dto.InvitationTokenData
import data.dto.MessageCode
import data.dto.ServerMessage
import data.dto.requests.AdminChangeEmailRequest
import data.dto.requests.CreateInvitationTokenRequest
import data.services.InvitationTokenService
import data.services.UserService
import io.javalin.http.Context
import io.javalin.http.bodyAsClass

class AdminController @Inject constructor(
    private val userService: UserService,
    private val invitationTokenService: InvitationTokenService,
) {
    fun getAdminStatus(ctx: Context) {
        val userId = ctx.attribute<Long>("userId")!!
        ctx.json(mapOf("isAdmin" to userService.isAdmin(userId)))
    }

    fun getInvitationTokens(ctx: Context) {
        val tokens = invitationTokenService.getAll().map { InvitationTokenData.from(it) }
        ctx.json(tokens)
    }

    fun createInvitationToken(ctx: Context) {
        val body = ctx.bodyAsClass<CreateInvitationTokenRequest>()
        val userId = ctx.attribute<Long>("userId")!!
        val token = invitationTokenService.createToken(userId, body.expiresInHours)
        ctx.json(InvitationTokenData.from(token))
    }

    fun deleteInvitationToken(ctx: Context) {
        val tokenId = ctx.pathParam("tokenId").toLong()
        if (invitationTokenService.delete(tokenId)) {
            ServerMessage(MessageCode.TokenDeleted).send(ctx)
        } else {
            ServerMessage(MessageCode.TokenNotFound).send(ctx)
        }
    }

    fun getUsers(ctx: Context) {
        ctx.json(userService.getAllWithStats())
    }

    fun deleteUser(ctx: Context) {
        val targetUserId = ctx.pathParam("userId").toLong()
        val currentUserId = ctx.attribute<Long>("userId")!!

        if (targetUserId == currentUserId) {
            ServerMessage(MessageCode.CannotDeleteSelf).send(ctx)
            return
        }

        userService.deleteUser(targetUserId)
        ServerMessage(MessageCode.UserDeleted).send(ctx)
    }

    fun changeUserEmail(ctx: Context) {
        val targetUserId = ctx.pathParam("userId").toLong()
        val body = ctx.bodyAsClass<AdminChangeEmailRequest>()

        if (userService.getById(targetUserId) == null) {
            ServerMessage(MessageCode.UserNotFound).send(ctx)
            return
        }

        val existingUser = userService.get(body.email)
        if (existingUser != null && existingUser.id != targetUserId) {
            ServerMessage(MessageCode.EmailAlreadyExists).send(ctx)
            return
        }

        userService.changeEmailAdmin(targetUserId, body.email)
        ServerMessage(MessageCode.AdminEmailChanged).send(ctx)
    }
}
