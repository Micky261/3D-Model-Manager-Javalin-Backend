package utils

import com.github.kittinunf.fuel.core.Request
import okhttp3.HttpUrl.Companion.toHttpUrl

fun String.toUrl() = this.toHttpUrl().toString()

fun Request.authToken(token: String) = this.header(mapOf("Authorization" to "Token $token"))
