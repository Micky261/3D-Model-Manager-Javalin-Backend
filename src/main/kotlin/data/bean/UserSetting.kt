package data.bean

import data.dto.UserSettingKey
import data.dto.UserSettingsType
import java.time.Instant

data class UserSetting(
    val userId: Long,
    val key: UserSettingKey,
    val type: UserSettingsType,
    val value: String,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
