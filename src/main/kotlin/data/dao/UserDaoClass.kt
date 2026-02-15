package data.dao

import com.google.inject.Inject
import data.dto.UserAdmin
import org.jdbi.v3.core.Jdbi

class UserDaoClass @Inject constructor(
    private val jdbi: Jdbi,
) {
    fun getAllWithStats(): List<UserAdmin> = jdbi.open().createQuery(
        """
            SELECT u.id, u.name, u.email, u.created_at,
                   COUNT(DISTINCT m.id) as model_count,
                   COALESCE(SUM(mf.size), 0) as storage_used
            FROM users u
            LEFT JOIN models m ON m.user_id = u.id
            LEFT JOIN model_files mf ON mf.user_id = u.id
            GROUP BY u.id, u.name, u.email, u.created_at
            ORDER BY u.created_at DESC
            """,
    )
        .map { rs, _ ->
            UserAdmin(
                id = rs.getLong("id"),
                name = rs.getString("name"),
                email = rs.getString("email"),
                createdAt = rs.getTimestamp("created_at").toInstant(),
                modelCount = rs.getLong("model_count"),
                storageUsed = rs.getLong("storage_used"),
            )
        }
        .toList()
}
