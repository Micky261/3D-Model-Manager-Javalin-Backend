package data.services

import com.google.inject.Inject
import data.bean.Session
import data.bean.User
import data.dao.SessionsDao
import data.dto.SessionData
import utils.randomAlphanumeric
import java.time.Instant

class SessionsService @Inject constructor(
    private val sessionsDao: SessionsDao,
) {
    companion object {
        const val SESSION_VALIDITY_SECONDS = 60L * 60L * 24L * 28L // 28 days
    }

    fun generateSessionId(): String = randomAlphanumeric(128)

    fun insert(userId: Long, sessionId: String) {
        sessionsDao.insert(userId, sessionId)
    }

    fun insert(userId: Long, sessionId: String, rights: String?) {
        sessionsDao.insert(userId, sessionId, rights)
    }

    fun get(sessionId: String): Session? = sessionsDao.get(
        sessionId,
        Instant.now().minusSeconds(SESSION_VALIDITY_SECONDS).epochSecond,
    )

    fun createSession(user: User): SessionData {
        val sessionId = generateSessionId()

        insert(user.id, sessionId, user.rights)

        return SessionData(sessionId)
    }
}
