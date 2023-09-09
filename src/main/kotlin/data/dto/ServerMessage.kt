package data.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import core.config.JacksonModule
import io.javalin.http.Context

data class ServerMessage(
    val messageCode: String,
    val message: String,
) {
    @JsonIgnore
    fun toJson(): String = JacksonModule.mapper.writeValueAsString(this)

    @JsonIgnore
    fun send(ctx: Context, httpStatusCode: Int) {
        ctx.status(httpStatusCode)
        ctx.json(this.toJson())
    }
}
