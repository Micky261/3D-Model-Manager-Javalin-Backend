package data.bean

import java.time.Instant

data class EmailVerification(
    val id: Long,
    val userId: Long,
    val token: String,
    val createdAt: Instant = Instant.now(),
)
