package data.dao

import com.google.inject.Inject
import data.bean.Model
import org.jdbi.v3.core.Jdbi

class ModelDaoClass @Inject constructor(
    private val jdbi: Jdbi,
) {
    fun search(userId: Long, searchFields: Set<String>, searchTerm: String): List<Model> {
        val whereString = searchFields.joinToString("OR") { "$it LIKE concat('%', :searchTerm, '%')" }

        return jdbi.open().createQuery("SELECT * FROM models WHERE user_id = :userId AND ($whereString);")
            .bind("userId", userId)
            .bind("searchTerm", searchTerm)
            .mapTo(Model::class.java)
            .toList()
    }
}
