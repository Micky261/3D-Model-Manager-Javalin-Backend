package backend.auth

import core.javalin.ControllerModule
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post

object AuthModule : ControllerModule() {
    override fun configure() {
        route<AuthController> { controller ->
            path("") {
                post("login", controller::login)
//                post("register", controller:register)
            }
        }

        route<VerificationController> { _ ->
            path("auth/email") {
//                get("resend", controller::resend)
//                get("verify/{id}/{hash}", controller:verify)
            }
        }
    }
}
