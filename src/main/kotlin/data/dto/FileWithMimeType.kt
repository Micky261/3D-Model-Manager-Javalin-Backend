package data.dto

import java.io.InputStream

data class FileWithMimeType(
    val file: InputStream,
    val mimeType: String,
)
