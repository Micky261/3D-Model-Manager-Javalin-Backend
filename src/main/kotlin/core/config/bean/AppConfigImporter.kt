package core.config.bean

data class AppConfigImporter(
    val printables: Boolean = true,
    val instructables: Boolean = false,
    val myminifactory: AppConfigApiKey?,
    val thingiverse: AppConfigApiKey?,
    val sketchfab: AppConfigApiKey?,
    val cults3d: AppConfigUsernamePassword?,
)
