package data.dao

import data.bean.UserSetting
import data.dto.UserSettingKey
import data.dto.UserSettingsType
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface UserSettingsDao {
    @SqlQuery("SELECT * FROM user_settings WHERE user_id = :userId AND type = :type")
    fun getSettingsByType(@Bind("userId") userId: Long, @Bind("type") type: UserSettingsType): List<UserSetting>

    @SqlUpdate(
        """
        INSERT INTO user_settings(user_id, `key`, type, value)
        VALUES (:userId, :key, :type, :value)
        ON DUPLICATE KEY UPDATE value = :value
    """,
    )
    fun updateSetting(@BindBean setting: UserSetting)

    @SqlQuery("SELECT * FROM user_settings WHERE user_id = :userId AND `key` = :key")
    fun getSettingByKey(@Bind("userId") userId: Long, @Bind("key") key: UserSettingKey): UserSetting?
}
