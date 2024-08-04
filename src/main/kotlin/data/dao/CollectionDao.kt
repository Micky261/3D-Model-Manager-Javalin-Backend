package data.dao

import data.bean.Collection
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface CollectionDao {
    @SqlUpdate("INSERT INTO collections (user_id, name, description) VALUES (:userId, :name, :description)")
    @GetGeneratedKeys("id")
    fun insert(@BindBean collection: Collection): Long

    @SqlQuery("SELECT * FROM collections WHERE user_id = :userId")
    fun getCollections(@Bind("userId") userId: Long): List<Collection>

    @SqlQuery("SELECT * FROM collections WHERE id = :id")
    fun get(@Bind("id") id: Long): Collection?

    @SqlUpdate(
        """
        UPDATE collections SET name = :name, description = :description, main_model = :mainModel 
        WHERE id = :id AND user_id = :userId
    """,
    )
    fun update(@BindBean collection: Collection)

    @SqlUpdate("DELETE FROM collections WHERE id = :id")
    fun delete(@Bind("id") id: Long)

    @SqlUpdate("INSERT INTO model_collections (collection_id, model_id) VALUES (:collectionId, :modelId)")
    fun insertRelation(@Bind("collectionId") collectionId: Long, @Bind("modelId") modelId: Long)

    @SqlQuery("SELECT model_id FROM model_collections WHERE collection_id = :collectionId")
    fun getModelsInCollection(@Bind("collectionId") collectionId: Long): List<Long>

    @SqlQuery("SELECT collection_id FROM model_collections WHERE model_id = :modelId")
    fun getCollectionsOfModel(@Bind("modelId") modelId: Long): List<Long>

    @SqlUpdate("DELETE FROM model_collections WHERE collection_id = :collectionId AND model_id = :modelId")
    fun deleteRelation(@Bind("collectionId") collectionId: Long, @Bind("modelId") modelId: Long)
}
