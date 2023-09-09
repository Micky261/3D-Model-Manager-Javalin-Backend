package core.config.bean.storage

data class AppConfigLocalStorage(
    override val name: String,
    override val url: String,
    override val capacity: String,
) : AppConfigStorage(name, url, capacity) {
    override val storageType = StorageType.Local
}
