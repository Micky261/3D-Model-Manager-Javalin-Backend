package data.dao

import data.bean.Session
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface SessionsDao {
    @SqlUpdate(
        """        
            INSERT INTO sessions (id, user_id, session_id, rights) VALUES (:id, :userId, :sessionI, :rights)
        """,
    )
    fun insert(
        @BindBean session: Session,
    )

    @SqlUpdate(
        """        
            INSERT INTO sessions (user_id, session_id) VALUES (:userId, :sessionId)
        """,
    )
    fun insert(
        @Bind("userId") userId: Long,
        @Bind("sessionId") sessionId: String,
    )

    @SqlQuery(
        """
            SELECT * FROM sessions WHERE session_id = :sessionId AND created_at > :sessionLimit
        """,
    )
    fun get(
        @Bind("sessionId") sessionId: String,
        @Bind("sessionLimit") sessionLimit: Long,
    ): Session?
}
