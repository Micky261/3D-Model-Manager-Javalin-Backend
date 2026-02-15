package data.dto

import java.time.Instant

data class UserAdmin(
    val id: Long,
    val name: String,
    val email: String,
    val createdAt: Instant,
    val modelCount: Long,
    val storageUsed: Long,
)
