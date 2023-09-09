package data.dto

import java.time.Instant

data class SessionDto(
    val sessionId: String,
    val sessionExpiry: Long = Instant.now().plusSeconds(60L * 60 * 24 * 80 - 60).epochSecond,
)
