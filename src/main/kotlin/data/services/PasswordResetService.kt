package data.services

import com.google.inject.Inject
import data.bean.PasswordReset
import data.dao.PasswordResetDao
import utils.randomAlphanumeric
import java.time.Instant

class PasswordResetService @Inject constructor(
    private val passwordResetDao: PasswordResetDao,
) {
    companion object {
        private const val TOKEN_LENGTH = 64
        const val TOKEN_VALIDITY_HOURS = 1L
    }

    fun createResetToken(email: String): String {
        passwordResetDao.deleteByEmail(email)

        val token = randomAlphanumeric(TOKEN_LENGTH)
        passwordResetDao.insert(email, token)

        return token
    }

    fun getByToken(token: String): PasswordReset? = passwordResetDao.getByToken(token)

    fun isTokenValid(reset: PasswordReset): Boolean {
        val expiryTime = reset.createdAt.plusSeconds(TOKEN_VALIDITY_HOURS * 60 * 60)
        return Instant.now().isBefore(expiryTime)
    }

    fun deleteByToken(token: String) = passwordResetDao.deleteByToken(token)
}
