package backend.version

import com.google.inject.Inject
import io.javalin.http.Context
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiContent
import io.javalin.openapi.OpenApiResponse

class VersionController @Inject constructor() {
    @OpenApi(
        path = "api/version",
        methods = [HttpMethod.GET],
        tags = ["bonus"],
        responses = [
            OpenApiResponse("200", [OpenApiContent(String::class, type = "text/plain")]),
            OpenApiResponse("400"),
            OpenApiResponse("403"),
        ],
    )
    fun getVersion(ctx: Context) = ctx.result("0.1.0")
}
