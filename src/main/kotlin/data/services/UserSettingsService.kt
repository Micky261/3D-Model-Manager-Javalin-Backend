package data.services

import com.google.inject.Inject
import data.bean.UserSetting
import data.dao.UserSettingsDao
import data.dto.UserSettingKey
import data.dto.UserSettingsType

class UserSettingsService @Inject constructor(
    private val userSettingsDao: UserSettingsDao,
) {
    fun getSettings(type: UserSettingsType, userId: Long): List<UserSetting> {
        return userSettingsDao.getSettingsByType(userId, type)
    }

    fun getSetting(userId: Long, key: UserSettingKey): UserSetting? {
        return userSettingsDao.getSettingByKey(userId, key)
    }

    fun saveSettings(settings: List<UserSetting>) {
        settings.forEach { userSettingsDao.updateSetting(it) }
    }
}
