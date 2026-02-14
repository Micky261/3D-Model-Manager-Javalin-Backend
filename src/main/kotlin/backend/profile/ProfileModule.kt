package backend.profile

import core.javalin.ControllerModule
import core.javalin.JavalinRole
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post

object ProfileModule : ControllerModule() {
    override fun configure() {
        route<ProfileController> { controller ->
            path("profile/") {
                get("me", controller::getProfile, JavalinRole.Authorized)
                post("change-password", controller::changePassword, JavalinRole.Authorized)
                post("change-name", controller::changeName, JavalinRole.Authorized)
                post("change-email", controller::changeEmail, JavalinRole.Authorized)
                get("settings/accounts", controller::getSettingsAccounts, JavalinRole.Authorized)
                post("settings/accounts", controller::setSettingsAccounts, JavalinRole.Authorized)
            }
        }

        route<ProfileStatsController> { controller ->
            path("profile/") {
                get("stats", controller::getProfileStatistics, JavalinRole.Authorized)
            }
        }
    }
}
