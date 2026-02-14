package data.dao

import data.bean.User
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

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
}
