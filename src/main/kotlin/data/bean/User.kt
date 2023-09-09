package data.bean

import java.time.Instant

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val emailVerifiedAt: Instant?,
    val password: String,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
