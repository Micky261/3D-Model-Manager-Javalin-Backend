package data.dao

import data.bean.User
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

@Suppress("ComplexInterface")
interface UserDao {
    @SqlUpdate(
        """
        INSERT INTO users (name, email, password)
        VALUES (:name, :email, :password)
        """,
    )
    @GetGeneratedKeys
    fun insert(
        @Bind("name") name: String,
        @Bind("email") email: String,
        @Bind("password") password: String,
    ): Long

    @SqlQuery("SELECT * FROM users WHERE email = :email")
    fun getUserByMail(email: String): User?

    @SqlQuery("SELECT * FROM users WHERE id = :id")
    fun getUserById(@Bind("id") id: Long): User?

    @SqlUpdate("UPDATE users SET email_verified_at = CURRENT_TIMESTAMP WHERE id = :id")
    fun setEmailVerified(@Bind("id") id: Long)

    @SqlUpdate("DELETE FROM users WHERE id = :id AND email_verified_at IS NULL")
    fun deleteUnverifiedUser(@Bind("id") id: Long): Int

    @SqlUpdate("UPDATE users SET rights = :rights WHERE id = :id")
    fun updateRights(@Bind("id") id: Long, @Bind("rights") rights: String?)

    @SqlQuery("SELECT COUNT(*) FROM users")
    fun count(): Long

    @SqlUpdate("UPDATE users SET password = :password WHERE id = :id")
    fun updatePassword(@Bind("id") id: Long, @Bind("password") password: String)

    @SqlUpdate("UPDATE users SET name = :name WHERE id = :id")
    fun updateName(@Bind("id") id: Long, @Bind("name") name: String)

    @SqlUpdate("UPDATE users SET pending_email = :email WHERE id = :id")
    fun setPendingEmail(@Bind("id") id: Long, @Bind("email") email: String)

    @SqlUpdate("UPDATE users SET pending_email = NULL WHERE id = :id")
    fun clearPendingEmail(@Bind("id") id: Long)

    @SqlUpdate("UPDATE users SET email = pending_email, pending_email = NULL WHERE id = :id")
    fun applyPendingEmail(@Bind("id") id: Long)
}
