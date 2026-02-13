package utils

import com.fasterxml.jackson.databind.JsonNode
import data.bean.FileType
import io.javalin.http.Context
import io.javalin.http.Header
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.InputStream
import java.security.SecureRandom

fun String.toUrl() = this.toHttpUrl().toString()

fun String.getFilenameWithExtensionFromUrl() = this.substringAfterLast("/").substringBeforeLast("?")

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

private val secureRandom = SecureRandom()
private val alphanumericChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun randomAlphanumeric(length: Int): String = (1..length)
    .map { alphanumericChars[secureRandom.nextInt(alphanumericChars.size)] }
    .joinToString("")
