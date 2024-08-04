package data.bean

import java.time.Instant

data class Collection(
    val id: Long,
    val userId: Long,
    val name: String,
    val description: String,
    val mainModel: Long? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
