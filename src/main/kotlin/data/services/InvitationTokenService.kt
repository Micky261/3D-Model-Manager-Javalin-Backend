package data.services

import com.google.inject.Inject
import data.bean.InvitationToken
import data.dao.InvitationTokenDao
import utils.randomAlphanumeric
import java.time.Instant

class InvitationTokenService @Inject constructor(
    private val invitationTokenDao: InvitationTokenDao,
) {
    companion object {
        private const val TOKEN_LENGTH = 64
    }

    fun createToken(createdBy: Long, expiresInHours: Long? = null): InvitationToken {
        val token = randomAlphanumeric(TOKEN_LENGTH)
        val expiresAt = expiresInHours?.let { Instant.now().plusSeconds(it * 3600) }
        val id = invitationTokenDao.insert(token, createdBy, expiresAt)
        return invitationTokenDao.getByToken(token)!!
    }

    fun validateToken(token: String): InvitationToken? {
        val invitationToken = invitationTokenDao.getByToken(token) ?: return null
        return if (invitationToken.isValid) invitationToken else null
    }

    fun markUsed(token: String, usedBy: Long): Boolean = invitationTokenDao.markUsed(token, usedBy) > 0

    fun getAll(): List<InvitationToken> = invitationTokenDao.getAll()

    fun delete(id: Long): Boolean = invitationTokenDao.delete(id) > 0
}
