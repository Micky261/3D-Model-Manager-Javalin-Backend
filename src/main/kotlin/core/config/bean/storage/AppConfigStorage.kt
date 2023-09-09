package core.config.bean.storage

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    Type(value = AppConfigLocalStorage::class, name = "local"),
    Type(value = AppConfigWebDavStorage::class, name = "webdav"),
)
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class AppConfigStorage(
    open val name: String,
    open val url: String,
    open val capacity: String,
    open val default: Boolean = false,
) {
    abstract val storageType: StorageType
}
