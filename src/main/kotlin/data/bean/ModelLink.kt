package data.bean

import java.time.Instant

data class ModelLink(
    val id: Long,
    val userId: Long,
    val modelId: Long,
    val title: String,
    val link: String,
    val description: String,
    val position: Long,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
