package data.bean

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val emailVerifiedAt: Instant?,
    val password: String,
    val rights: String? = null,
    val pendingEmail: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) {
    @JsonIgnore
    val rightsList: List<AppRight> = rights?.split(",")?.map { AppRight.valueOf(it) } ?: emptyList()

    @JsonIgnore
    val isAdmin: Boolean = AppRight.Admin in rightsList
}
