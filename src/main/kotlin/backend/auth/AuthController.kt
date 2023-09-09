package backend.auth

import com.google.inject.Inject
import data.dto.LoginDto
import data.dto.ServerMessage
import data.services.SessionsService
import data.services.UserService
import io.javalin.http.Context
import io.javalin.http.bodyAsClass

class AuthController @Inject constructor(
    private val sessionsService: SessionsService,
    private val userService: UserService,
) {
    fun login(ctx: Context) {
        val body = ctx.bodyAsClass<LoginDto>()

        val user = userService.checkLogin(body.email, body.password)
        if (user != null) {
            ctx.json(sessionsService.createSession(user))
        } else {
            ServerMessage("LOGIN_ERROR", "Error on Login").send(ctx, 405)
        }
    }
}
