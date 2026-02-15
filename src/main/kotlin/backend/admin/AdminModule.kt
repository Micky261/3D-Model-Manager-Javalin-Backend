package backend.admin

import core.javalin.ControllerModule
import core.javalin.JavalinRole
import io.javalin.apibuilder.ApiBuilder.delete
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.apibuilder.ApiBuilder.put

object AdminModule : ControllerModule() {
    override fun configure() {
        route<AdminController> { controller ->
            path("admin/") {
                get("status", controller::getAdminStatus, JavalinRole.Authorized)
                get("users", controller::getUsers, JavalinRole.AdminOnly)
                put("users/{userId}/email", controller::changeUserEmail, JavalinRole.AdminOnly)
                delete("users/{userId}", controller::deleteUser, JavalinRole.AdminOnly)
                get("invitation-tokens", controller::getInvitationTokens, JavalinRole.AdminOnly)
                post("invitation-tokens", controller::createInvitationToken, JavalinRole.AdminOnly)
                delete("invitation-tokens/{tokenId}", controller::deleteInvitationToken, JavalinRole.AdminOnly)
            }
        }
    }
}
