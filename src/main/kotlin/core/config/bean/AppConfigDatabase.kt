package core.config.bean

data class AppConfigDatabase(
    val db: String,
    val host: String,
    val port: Int,
    val name: String,
    val username: String,
    val password: String,
)
