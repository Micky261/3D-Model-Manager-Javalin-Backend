package data.bean

import java.time.Instant

data class InvitationToken(
    val id: Long,
    val token: String,
    val createdBy: Long,
    val usedBy: Long? = null,
    val expiresAt: Instant? = null,
    val usedAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
) {
    val isUsed: Boolean get() = usedBy != null
    val isExpired: Boolean get() = expiresAt != null && Instant.now().isAfter(expiresAt)
    val isValid: Boolean get() = !isUsed && !isExpired
}
