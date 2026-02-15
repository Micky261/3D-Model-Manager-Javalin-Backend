package core.javalin

import data.bean.Session
import io.javalin.http.Context

fun Context.session(): Session = this.attribute("session")!!
fun Context.sessionId(): String = this.attribute("sessionId")!!
fun Context.userId(): Long = this.attribute("userId")!!
fun Context.modelId(): Long = this.attribute("modelId")!!
fun Context.locale(): String = this.header("Accept-Language")
    ?.takeIf { it in listOf("en-GB", "de-DE") }
    ?: "en-GB"
