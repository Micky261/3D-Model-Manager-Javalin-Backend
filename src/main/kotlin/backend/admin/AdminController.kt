package backend.admin

import com.google.inject.Inject
import data.dto.CreateInvitationTokenRequest
import data.dto.InvitationTokenDto
import data.dto.MessageCode
import data.dto.ServerMessage
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
        val tokens = invitationTokenService.getAll().map { InvitationTokenDto.from(it) }
        ctx.json(tokens)
    }

    fun createInvitationToken(ctx: Context) {
        val body = ctx.bodyAsClass<CreateInvitationTokenRequest>()
        val userId = ctx.attribute<Long>("userId")!!
        val token = invitationTokenService.createToken(userId, body.expiresInHours)
        ctx.json(InvitationTokenDto.from(token))
    }

    fun deleteInvitationToken(ctx: Context) {
        val tokenId = ctx.pathParam("tokenId").toLong()
        if (invitationTokenService.delete(tokenId)) {
            ServerMessage(MessageCode.TokenDeleted).send(ctx)
        } else {
            ServerMessage(MessageCode.TokenNotFound).send(ctx)
        }
    }
}
