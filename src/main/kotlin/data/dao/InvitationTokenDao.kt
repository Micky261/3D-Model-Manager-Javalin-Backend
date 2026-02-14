package data.dao

import data.bean.InvitationToken
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface InvitationTokenDao {
    @SqlUpdate(
        """
        INSERT INTO invitation_tokens (token, created_by, expires_at)
        VALUES (:token, :createdBy, :expiresAt)
        """,
    )
    @GetGeneratedKeys
    fun insert(
        @Bind("token") token: String,
        @Bind("createdBy") createdBy: Long,
        @Bind("expiresAt") expiresAt: java.time.Instant?,
    ): Long

    @SqlQuery("SELECT * FROM invitation_tokens WHERE token = :token")
    fun getByToken(@Bind("token") token: String): InvitationToken?

    @SqlQuery("SELECT * FROM invitation_tokens ORDER BY created_at DESC")
    fun getAll(): List<InvitationToken>

    @SqlUpdate(
        """
        UPDATE invitation_tokens SET used_by = :usedBy, used_at = CURRENT_TIMESTAMP
        WHERE token = :token AND used_by IS NULL
        """,
    )
    fun markUsed(@Bind("token") token: String, @Bind("usedBy") usedBy: Long): Int

    @SqlUpdate("DELETE FROM invitation_tokens WHERE id = :id")
    fun delete(@Bind("id") id: Long): Int
}
