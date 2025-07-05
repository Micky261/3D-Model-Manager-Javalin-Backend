package utils

import com.fasterxml.jackson.databind.JsonNode
import com.github.kittinunf.fuel.core.Request
import data.bean.FileType
import data.importer.BaseImporter
import io.javalin.http.Context
import io.javalin.http.Header
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.InputStream

fun String.toUrl() = this.toHttpUrl().toString()

// fun String.getFilenameFromUrl() = this.substringAfterLast("/").substringBeforeLast(".")
fun String.getFilenameWithExtensionFromUrl() = this.substringAfterLast("/").substringBeforeLast("?")

fun Request.authToken(token: String) = this.header(mapOf("Authorization" to "Token $token"))

fun Request.ua() = this.header("User-Agent", BaseImporter.USER_AGENT)

fun Context.fileResponse(file: InputStream, filename: String) {
    this.contentType(FileType.getMimeTypeFromFilename(filename))
    this.result(file)
    this.header(Header.CONTENT_DISPOSITION, "filename=$filename")
}

fun JsonNode.getText(field: String) = this.get(field).asText()!!
fun JsonNode.getLong(field: String) = this.get(field).asLong()
fun JsonNode.getBoolean(field: String) = this.get(field).asBoolean()
