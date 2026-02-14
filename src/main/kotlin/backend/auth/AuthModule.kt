package backend.auth

import core.javalin.ControllerModule
import core.javalin.JavalinRole
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post

object AuthModule : ControllerModule() {
    override fun configure() {
        route<AuthController> { controller ->
            path("") {
                post("login", controller::login)
                post("register", controller::register)
                get("registration-info", controller::getRegistrationInfo)
                post("request-password-reset", controller::requestPasswordReset)
                post("reset-password", controller::resetPassword)
            }
        }

        route<VerificationController> { controller ->
            path("auth/email") {
                post("resend", controller::resend, JavalinRole.Authorized)
                get("verify/{token}", controller::verify)
            }
        }
    }
}
