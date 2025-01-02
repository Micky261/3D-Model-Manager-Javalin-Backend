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
                get("settings/accounts", controller::getSettingsAccounts, JavalinRole.Authorized)
                post("settings/accounts", controller::setSettingsAccounts, JavalinRole.Authorized)
            }
        }
    }
}
