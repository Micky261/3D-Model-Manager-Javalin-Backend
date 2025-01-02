package backend.profile

import com.google.inject.Inject
import core.javalin.userId
import data.dto.UserSettingDto
import data.dto.UserSettingsType
import data.services.UserSettingsService
import io.javalin.http.Context
import io.javalin.http.bodyAsClass

class ProfileController @Inject constructor(
    private val userSettingsService: UserSettingsService,
) {
    fun getSettingsAccounts(ctx: Context) {
        ctx.json(userSettingsService.getSettings(UserSettingsType.Account, ctx.userId()))
    }

    fun setSettingsAccounts(ctx: Context) {
        val receivedSettings = ctx.bodyAsClass<List<UserSettingDto>>()
            .map { it.toUserSetting(ctx.userId()) }

        userSettingsService.saveSettings(receivedSettings)

        ctx.json(userSettingsService.getSettings(UserSettingsType.Account, ctx.userId()))
    }
}
