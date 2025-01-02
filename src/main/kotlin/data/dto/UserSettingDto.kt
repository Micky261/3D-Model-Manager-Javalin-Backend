package data.dto

import data.bean.UserSetting

data class UserSettingDto(
    val key: UserSettingKey,
    val type: UserSettingsType,
    val value: String,
) {
    fun toUserSetting(userId: Long): UserSetting = UserSetting(userId, key, type, value)
}
