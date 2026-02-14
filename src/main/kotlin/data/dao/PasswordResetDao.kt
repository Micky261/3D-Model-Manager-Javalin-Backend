package data.dao

import data.bean.PasswordReset
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface PasswordResetDao {
    @SqlUpdate(
        """
        INSERT INTO password_resets (email, token)
        VALUES (:email, :token)
        """,
    )
    fun insert(
        @Bind("email") email: String,
        @Bind("token") token: String,
    )

    @SqlQuery("SELECT * FROM password_resets WHERE token = :token")
    fun getByToken(@Bind("token") token: String): PasswordReset?

    @SqlUpdate("DELETE FROM password_resets WHERE email = :email")
    fun deleteByEmail(@Bind("email") email: String)

    @SqlUpdate("DELETE FROM password_resets WHERE token = :token")
    fun deleteByToken(@Bind("token") token: String)

    @SqlUpdate("DELETE FROM password_resets WHERE created_at < :expiryTime")
    fun deleteExpired(@Bind("expiryTime") expiryTime: Long): Int
}
