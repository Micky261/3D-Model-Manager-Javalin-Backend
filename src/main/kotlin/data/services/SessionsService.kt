package data.services

import com.google.inject.Inject
import data.bean.Session
import data.bean.User
import data.dao.SessionsDao
import data.dto.SessionDto
import org.apache.commons.lang3.RandomStringUtils
import java.time.Instant

class SessionsService @Inject constructor(
    private val sessionsDao: SessionsDao,
) {
    fun generateSessionId(): String {
        return RandomStringUtils.randomAlphanumeric(128)
    }

    fun insert(userId: Long, sessionId: String) {
        sessionsDao.insert(userId, sessionId)
    }

    fun get(sessionId: String): Session? {
        return sessionsDao.get(sessionId, Instant.now().minusSeconds(60L * 60L * 24L * 28L).epochSecond)
    }

    fun createSession(user: User): SessionDto {
        val sessionId = generateSessionId()

        insert(user.id, sessionId)

        return SessionDto(sessionId)
    }
}
