package data.dao

import data.bean.ModelLink
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface ModelLinkDao {
    @SqlUpdate(
        """        
        INSERT INTO model_links (
            user_id, model_id, title, link, description, position
        ) VALUES ( 
            :userId, :modelId, :title, :link, :description, :position
        )
        """,
    )
    @GetGeneratedKeys("id")
    fun insert(
        @BindBean modelLink: ModelLink,
    ): Long

    @SqlQuery(
        """
            SELECT * FROM model_links WHERE id = :id
        """,
    )
    fun get(
        @Bind("id") id: Long,
    ): ModelLink?

    @SqlQuery(
        """
            SELECT * FROM model_links WHERE model_id = :modelId AND user_id = :userId
        """,
    )
    fun getAllByModel(
        @Bind("modelId") modelId: Long,
        @Bind("userId") userId: Long,
    ): List<ModelLink>

    @SqlUpdate(
        """
            UPDATE model_links SET title = :title, link = :link, description = :description, position = :position WHERE id = :id AND model_id = :modelId AND user_id = :userId
        """,
    )
    fun update(
        @BindBean modelLink: ModelLink,
    )

    @SqlUpdate(
        """
            DELETE FROM model_links WHERE id = :id AND user_id = :userId
        """,
    )
    fun delete(
        @Bind("id") id: Long,
        @Bind("userId") userId: Long,
    )
}
