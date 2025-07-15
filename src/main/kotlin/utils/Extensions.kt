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

fun Long.formatFileSize(): String {
    if (this == 0L) {
        return "0 B"
    }

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val k = 1024.0
    var value = this.toDouble()
    var unitIndex = 0

    while (value >= k && unitIndex < units.size - 1) {
        value /= k
        unitIndex++
    }

    return if (unitIndex == 0) {
        String.format("%d %s", this, units[unitIndex])
    } else {
        String.format("%.2f %s", value, units[unitIndex])
    }
}
