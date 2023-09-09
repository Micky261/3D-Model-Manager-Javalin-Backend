package data.bean

import java.time.Instant

data class ModelFile(
    val id: Long,
    val storage: String,
    val userId: Long,
    val modelId: Long,
    val type: ModelFileType,
    val filename: String,
    val position: Long,
    val size: Long,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
