package core.config.bean.storage

data class AppConfigWebDavStorage(
    override val name: String,
    override val url: String,
    override val capacity: String,
    val username: String,
    val password: String,
) : AppConfigStorage(name, url, capacity) {
    override val storageType = StorageType.WebDav
}
