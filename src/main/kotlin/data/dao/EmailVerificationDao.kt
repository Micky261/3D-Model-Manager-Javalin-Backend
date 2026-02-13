package data.dao

import data.bean.EmailVerification
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface EmailVerificationDao {
    @SqlUpdate(
        """
        INSERT INTO email_verifications (user_id, token)
        VALUES (:userId, :token)
        """,
    )
    fun insert(
        @Bind("userId") userId: Long,
        @Bind("token") token: String,
    )

    @SqlQuery("SELECT * FROM email_verifications WHERE token = :token")
    fun getByToken(@Bind("token") token: String): EmailVerification?

    @SqlQuery("SELECT * FROM email_verifications WHERE user_id = :userId ORDER BY created_at DESC LIMIT 1")
    fun getByUserId(@Bind("userId") userId: Long): EmailVerification?

    @SqlUpdate("DELETE FROM email_verifications WHERE user_id = :userId")
    fun deleteByUserId(@Bind("userId") userId: Long)

    @SqlUpdate("DELETE FROM email_verifications WHERE token = :token")
    fun deleteByToken(@Bind("token") token: String)

    @SqlUpdate("DELETE FROM email_verifications WHERE created_at < :expiryTime")
    fun deleteExpired(@Bind("expiryTime") expiryTime: Long): Int

    @SqlQuery("SELECT user_id FROM email_verifications WHERE created_at < :expiryTime")
    fun getExpiredUserIds(@Bind("expiryTime") expiryTime: Long): List<Long>
}
