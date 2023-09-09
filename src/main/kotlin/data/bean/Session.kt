package data.bean

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant

data class Session(
    val id: Long,
    val userId: Long,
    val sessionId: String,
    val rights: String?,
    val createdAt: Instant = Instant.now(),
) {
    @JsonIgnore
    val rightsList: List<AppRight> = rights?.split(",")?.map { AppRight.valueOf(it) } ?: emptyList()
}
