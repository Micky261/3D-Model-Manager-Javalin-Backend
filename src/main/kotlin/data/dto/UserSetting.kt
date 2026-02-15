package data.dto

data class UserSetting(
    val key: UserSettingKey,
    val type: UserSettingsType,
    val value: String,
) {
    fun toUserSetting(userId: Long): data.bean.UserSetting = data.bean.UserSetting(userId, key, type, value)
}
