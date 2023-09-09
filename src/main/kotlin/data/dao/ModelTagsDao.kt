package data.dao

import data.bean.ModelTag
import data.dto.ModelTagCount
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface ModelTagsDao {
    @SqlUpdate(
        """        
        INSERT INTO model_tags (
            user_id, model_id, tag
        ) VALUES ( 
            :userId, :modelId, :tag
        )
        """,
    )
    fun insert(
        @BindBean modelTag: ModelTag,
    )

    @SqlQuery(
        """
            SELECT * FROM model_tags WHERE user_id = :userId
        """,
    )
    fun get(
        @Bind("userId") userId: Long,
    ): List<ModelTag>

    @SqlQuery(
        """
            SELECT tag, COUNT(tag) as count FROM model_tags WHERE user_id = :userId GROUP BY tag ORDER BY count
        """,
    )
    fun getTagWithCount(
        @Bind("userId") userId: Long,
    ): List<ModelTagCount>

    @SqlQuery(
        """
            SELECT * FROM model_tags WHERE user_id = :userId AND model_id = :modelId
        """,
    )
    fun get(
        @Bind("userId") userId: Long,
        @Bind("modelId") modelId: Long,
    ): List<ModelTag>

    @SqlQuery(
        """
            SELECT * FROM model_tags WHERE user_id = :userId AND model_id = :modelId AND tag = :tag
        """,
    )
    fun get(
        @Bind("userId") userId: Long,
        @Bind("modelId") modelId: Long,
        @Bind("tag") tag: String,
    ): ModelTag?

    @SqlUpdate(
        """
            DELETE FROM model_tags WHERE user_id = :userId AND model_id = :modelId AND tag = :tag
        """,
    )
    fun delete(
        @BindBean modelTag: ModelTag,
    )
}
