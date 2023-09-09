package core.javalin

import com.google.inject.Inject
import data.dto.ServerMessage
import data.services.AccessService
import data.services.SessionsService
import io.javalin.http.Context
import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.UnauthorizedResponse
import io.javalin.http.pathParamAsClass
import io.javalin.http.queryParamAsClass

class AppAccessManager @Inject constructor(
    private val sessionsService: SessionsService,
    private val accessService: AccessService,
) {
    fun handle(ctx: Context) {
        val routeRoles = ctx.routeRoles()

        when {
            routeRoles.isEmpty() || routeRoles == setOf(JavalinRole.Unauthorized) -> return

            routeRoles.intersect(setOf(JavalinRole.Authorized, JavalinRole.ModelOwnerOnly)).isNotEmpty() -> {
                val sessionId = ctx.queryParamAsClass<String>("3DMM_Session").get()
                val session = sessionsService.get(sessionId)

                if (session != null) {
                    ctx.attribute("session", session)
                    ctx.attribute("sessionId", session.sessionId)
                    ctx.attribute("userId", session.userId)

                    if (routeRoles == setOf(JavalinRole.ModelOwnerOnly)) {
                        val modelId = ctx.pathParamAsClass<Long>("modelId").get()
                        ctx.attribute("modelId", modelId)

                        if (!accessService.userOwnsModel(session.userId, modelId)) {
                            ServerMessage("AUTH_ERROR", "Error on Auth").send(ctx, 405)
                            throw UnauthorizedResponse()
                        }
                    }
                } else {
                    ServerMessage("AUTH_ERROR", "Error on Auth").send(ctx, 405)
                    throw UnauthorizedResponse()
                }
            }

            else -> throw InternalServerErrorResponse("Unknown role")
        }
    }

    companion object {
        const val SESSION_COOKIE_NAME = "3DMMSession" // TODO implement cookie access
    }
}
