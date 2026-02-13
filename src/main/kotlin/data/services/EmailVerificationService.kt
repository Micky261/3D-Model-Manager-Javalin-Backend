package data.services

import com.google.inject.Inject
import data.bean.EmailVerification
import data.dao.EmailVerificationDao
import data.dao.UserDao
import utils.randomAlphanumeric
import java.time.Instant

class EmailVerificationService @Inject constructor(
    private val emailVerificationDao: EmailVerificationDao,
    private val userDao: UserDao,
) {
    companion object {
        private const val TOKEN_LENGTH = 64
        const val TOKEN_VALIDITY_HOURS = 24L
    }

    fun generateToken(): String = randomAlphanumeric(TOKEN_LENGTH)

    fun createVerificationToken(userId: Long): String {
        emailVerificationDao.deleteByUserId(userId)

        val token = generateToken()
        emailVerificationDao.insert(userId, token)

        return token
    }

    fun getByToken(token: String): EmailVerification? = emailVerificationDao.getByToken(token)

    fun isTokenValid(verification: EmailVerification): Boolean {
        val expiryTime = verification.createdAt.plusSeconds(TOKEN_VALIDITY_HOURS * 60 * 60)
        return Instant.now().isBefore(expiryTime)
    }

    fun verifyEmail(token: String): Boolean {
        val verification = getByToken(token) ?: return false

        if (!isTokenValid(verification)) {
            emailVerificationDao.deleteByToken(token)
            return false
        }

        userDao.setEmailVerified(verification.userId)
        emailVerificationDao.deleteByUserId(verification.userId)

        return true
    }

    fun getVerificationForUser(userId: Long): EmailVerification? = emailVerificationDao.getByUserId(userId)
}
