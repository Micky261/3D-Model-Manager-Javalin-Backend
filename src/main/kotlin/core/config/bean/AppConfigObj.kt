package core.config.bean

import core.config.bean.storage.AppConfigStorage

data class AppConfigObj(
    val general: AppConfigGeneral,
    val database: AppConfigDatabase,
    val mail: AppConfigMail,
    val importer: AppConfigImporter,
    val storage: List<AppConfigStorage>,
)
