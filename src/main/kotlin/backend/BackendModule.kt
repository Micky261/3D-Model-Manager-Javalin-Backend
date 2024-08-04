package backend

import backend.auth.AuthModule
import backend.collections.CollectionModule
import backend.files.FilesModule
import backend.importer.ImporterModule
import backend.models.ModelsModule
import backend.search.SearchModule
import backend.tags.TagsModule
import backend.version.VersionModule
import dev.misfitlabs.kotlinguice4.KotlinModule

object BackendModule : KotlinModule() {
    override fun configure() {
        install(AuthModule)
        install(CollectionModule)
        install(FilesModule)
        install(ImporterModule)
        install(ModelsModule)
        install(SearchModule)
        install(TagsModule)
        install(VersionModule)
    }
}
