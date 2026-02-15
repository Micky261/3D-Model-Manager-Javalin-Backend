package data.dao

import data.bean.Model
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

@Suppress("ComplexInterface")
interface ModelDao {
    @SqlUpdate(
        """        
        INSERT INTO models (
            user_id, name, description, notes, favorite, author, licence, imported_name, imported_description, imported_author, imported_licence, import_source
        ) VALUES ( 
            :userId, :name, :description, :notes, :favorite, :author, :licence, :importedName, :importedDescription, :importedAuthor, :importedLicence, :importSource
        )
        """,
    )
    @GetGeneratedKeys("id")
    fun insert(
        @BindBean model: Model,
    ): Long

    @SqlQuery(
        """
            SELECT * FROM models WHERE user_id = :userId
        """,
    )
    fun getAllByUser(
        @Bind("userId") userId: Long,
    ): List<Model>

    @SqlQuery(
        """
            SELECT * FROM models WHERE user_id = :userId ORDER BY id DESC LIMIT :count
        """,
    )
    fun getNewestByUser(
        @Bind("userId") userId: Long,
        @Bind("count") count: Int,
    ): List<Model>

    @SqlQuery(
        """
            SELECT * FROM models WHERE user_id = :userId AND favorite = 1
        """,
    )
    fun getFavoritesByUser(@Bind("userId") userId: Long): List<Model>

    @SqlQuery(
        """
            SELECT * FROM models WHERE user_id = :userId ORDER BY RAND() LIMIT :count
        """,
    )
    fun getRandomByUser(
        @Bind("userId") userId: Long,
        @Bind("count") count: Int,
    ): List<Model>

    @SqlQuery("SELECT * FROM models WHERE id = :id")
    fun get(@Bind("id") id: Long): Model?

    @SqlQuery("SELECT * FROM models WHERE id = :id AND user_id = :userId")
    fun get(@Bind("id") id: Long, @Bind("userId") userId: Long): Model?

    @SqlUpdate(
        """
            UPDATE models SET name = :name, description = :description, favorite = :favorite, author = :author, notes = :notes, licence = :licence WHERE id = :id AND user_id = :userId
        """,
    )
    fun update(
        @BindBean model: Model,
    )

    @SqlUpdate(
        """
            UPDATE models
            SET name = :name, description = :description, favorite = :favorite, author = :author, notes = :notes, licence = :licence, 
            imported_name = :importedName, imported_description = :importedDescription, 
            imported_author = :importedAuthor, imported_licence = :importedLicence, import_source = :importSource
            WHERE id = :id AND user_id = :userId
        """,
    )
    fun dangerousUpdate(
        @BindBean model: Model,
    )

    @SqlUpdate("DELETE FROM models WHERE id = :id AND user_id = :userId")
    fun delete(@Bind("id") id: Long, @Bind("userId") userId: Long)

    @SqlQuery(
        """
            SELECT * FROM models WHERE user_id = :userId AND (
                name LIKE :searchString OR
                description LIKE :searchString OR
                notes LIKE :searchString OR
                author LIKE :searchString OR
                licence LIKE :searchString OR
                imported_name LIKE :searchString OR
                imported_description LIKE :searchString OR
                imported_author LIKE :searchString OR
                imported_licence LIKE :searchString OR
                import_source LIKE :searchString
            )
        """,
    )
    fun search(
        @Bind("userId") userId: Long,
        @Bind("searchString") searchString: String,
    ): List<Model>
}
