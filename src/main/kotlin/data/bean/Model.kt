package data.bean

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class Model(
    val id: Long,
    val userId: Long,
    val name: String,
    val importedName: String? = null,
    val description: String,
    val importedDescription: String? = null,
    val notes: String,
    val favorite: Boolean,
    val author: String,
    val importedAuthor: String? = null,
    val licence: String,
    val importedLicence: String? = null,
    val importSource: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) {
    companion object {
        val searchableFields = setOf(
            "name", "description", "notes", "author", "licence",
            "imported_name", "imported_description", "imported_author", "imported_licence", "import_source",
        )
    }
}
