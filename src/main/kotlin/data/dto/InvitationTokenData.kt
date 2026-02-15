package data.dto

import data.bean.InvitationToken
import java.time.Instant

data class InvitationTokenData(
    val id: Long,
    val token: String,
    val createdBy: Long,
    val usedBy: Long?,
    val expiresAt: Instant?,
    val usedAt: Instant?,
    val createdAt: Instant,
    val isUsed: Boolean,
    val isExpired: Boolean,
    val isValid: Boolean,
) {
    companion object {
        fun from(token: InvitationToken): InvitationTokenData = InvitationTokenData(
            id = token.id,
            token = token.token,
            createdBy = token.createdBy,
            usedBy = token.usedBy,
            expiresAt = token.expiresAt,
            usedAt = token.usedAt,
            createdAt = token.createdAt,
            isUsed = token.isUsed,
            isExpired = token.isExpired,
            isValid = token.isValid,
        )
    }
}
