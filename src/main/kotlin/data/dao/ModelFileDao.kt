package data.dao

import data.bean.ModelFile
import data.bean.ModelFileType
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.customizer.BindList
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

@Suppress("ComplexInterface")
interface ModelFileDao {
    @SqlUpdate(
        """        
        INSERT INTO model_files (
            user_id, model_id, storage, type, filename, position, size
        ) VALUES ( 
            :userId, :modelId, :storage, :type, :filename, :position, :size
        )
        """,
    )
    @GetGeneratedKeys("id")
    fun insert(
        @BindBean modelFile: ModelFile,
    ): Long

    @SqlQuery("SELECT COALESCE(SUM(size),0) as size_acc FROM model_files WHERE storage = :storageName")
    fun getStorageSize(storageName: String): Long

    @SqlQuery(
        """
         SELECT * FROM model_files WHERE model_id = :modelId AND user_id = :userId AND type = :type
      """,
    )
    fun get(
        @Bind("modelId") modelId: Long,
        @Bind("userId") userId: Long,
        @Bind("type") type: ModelFileType,
    ): List<ModelFile>

    @SqlQuery(
        """
         SELECT * FROM model_files WHERE model_id = :modelId AND user_id = :userId AND id IN (<ids>)
      """,
    )
    fun get(
        @Bind("modelId") modelId: Long,
        @Bind("userId") userId: Long,
        @BindList("ids") ids: List<Long>,
    ): List<ModelFile>

    @SqlQuery(
        """
         SELECT * FROM model_files WHERE model_id = :modelId AND user_id = :userId
      """,
    )
    fun get(
        @Bind("modelId") modelId: Long,
        @Bind("userId") userId: Long,
    ): List<ModelFile>

    @SqlQuery(
        """
         SELECT * FROM model_files WHERE id = :fileId AND user_id = :userId
      """,
    )
    fun getFileByUser(
        @Bind("userId") userId: Long,
        @Bind("fileId") fileId: Long,
    ): ModelFile?

    @SqlQuery(
        """
         SELECT * FROM model_files WHERE model_id = :modelId AND user_id = :userId AND type = 'image' ORDER BY position LIMIT 1
      """,
    )
    fun getMainImage(
        @Bind("modelId") modelId: Long,
        @Bind("userId") userId: Long,
    ): ModelFile?

    @SqlUpdate(
        """
         DELETE FROM model_files WHERE id = :fileId
      """,
    )
    fun delete(
        @Bind("fileId") fileId: Long,
    )

    @SqlUpdate(
        """
         UPDATE model_files SET position = :position, type = :type, filename = :filename WHERE id = :fileId
      """,
    )
    fun update(
        @Bind("fileId") fileId: Long,
        @Bind("position") position: Long,
        @Bind("type") type: ModelFileType,
        @Bind("filename") filename: String,
    )

    @SqlQuery(
        """
            SELECT COUNT(id) as c FROM model_files WHERE model_id = :modelId AND user_id = :userId AND type = :type AND filename = :filename
        """,
    )
    fun checkDuplicate(
        @Bind("modelId") modelId: Long,
        @Bind("userId") userId: Long,
        @Bind("type") type: ModelFileType,
        @Bind("filename") filename: String,
    ): Long
}
