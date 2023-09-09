package data.bean

import java.time.Instant

data class PasswordReset(
    val email: String,
    val token: String,
    val createdAt: Instant = Instant.now(),
)
