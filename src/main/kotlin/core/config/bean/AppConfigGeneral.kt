package core.config.bean

data class AppConfigGeneral(
    val enableCors: Boolean,
    val corsAllowedHosts: List<String> = emptyList(),
    val baseDir: String,
    val serverPort: Int,
)
