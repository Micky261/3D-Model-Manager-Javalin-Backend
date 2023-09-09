package data.bean

import java.time.Instant

data class ModelFull(
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
    val createdAt: Instant,
    val updatedAt: Instant,
    val links: List<ModelLink>,
    val tags: List<String>,
) {
    companion object {
        fun from(model: Model, links: List<ModelLink>, tags: List<String>): ModelFull {
            return ModelFull(
                model.id,
                model.userId,
                model.name,
                model.importedName,
                model.description,
                model.importedDescription,
                model.notes,
                model.favorite,
                model.author,
                model.importedAuthor,
                model.licence,
                model.importedLicence,
                model.importSource,
                model.createdAt,
                model.updatedAt,
                links,
                tags,
            )
        }
    }
}
