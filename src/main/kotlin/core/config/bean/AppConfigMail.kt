package core.config.bean

data class AppConfigMail(
    val mailer: String,
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val encryption: String,
    val from_address: String,
    val from_name: String,
)
