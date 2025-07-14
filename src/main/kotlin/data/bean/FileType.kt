package data.bean

object FileType {
    val types = mapOf(
        "3gp" to "video/3gpp",
        "3mf" to "model/3mf",
        "7z" to "application/x-7z-compressed",
        "avi" to "video/x-msvideo",
        "bmp" to "image/x-ms-bmp",
        "csv" to "text/csv",
        "doc" to "application/msword",
        "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "epub" to "application/epub+zip",
        "gif" to "image/gif",
        "htm" to "text/html",
        "html" to "text/html",
        "ico" to "image/vnd.microsoft.icon",
        "iges" to "model/iges",
        "igs" to "model/iges",
        "jpe" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "jpg" to "image/jpeg",
        "json" to "application/json",
        "md" to "text/markdown",
        "mesh" to "model/mesh",
        "mkv" to "video/x-matroska",
        "mov" to "video/quicktime",
        "mp2" to "audio/mpeg",
        "mp2a" to "audio/mpeg",
        "mp3" to "audio/mpeg",
        "mp4" to "video/mp4",
        "mp4a" to "audio/mp4",
        "mpeg" to "video/mpeg",
        "mpg" to "video/mpeg",
        "mpg4" to "video/mp4",
        "msi" to "application/x-msdownload",
        "obj" to "application/octet-stream",
        "odb" to "application/vnd.oasis.opendocument.database",
        "odc" to "application/vnd.oasis.opendocument.chart",
        "odf" to "application/vnd.oasis.opendocument.formula",
        "odft" to "application/vnd.oasis.opendocument.formula-template",
        "odg" to "application/vnd.oasis.opendocument.graphics",
        "odi" to "application/vnd.oasis.opendocument.image",
        "odp" to "application/vnd.oasis.opendocument.presentation",
        "ods" to "application/vnd.oasis.opendocument.spreadsheet",
        "odt" to "application/vnd.oasis.opendocument.text",
        "pdf" to "application/pdf",
        "png" to "image/png",
        "ppt" to "application/vnd.ms-powerpoint",
        "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "psd" to "image/x-photoshop",
        "rar" to "application/rar",
        "stl" to "application/sla",
        "tex" to "text/x-tex",
        "tif" to "image/tiff",
        "tiff" to "image/tiff",
        "txt" to "text/plain",
        "wav" to "audio/x-wav",
        "webm" to "video/webm",
        "xls" to "application/vnd.ms-excel",
        "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "xml" to "application/xml",
        "xsl" to "application/xml",
        "zip" to "application/zip",
    )

    val applications = listOf(
        "pdf" to listOf("pdf"),
        "image" to listOf("png", "tif", "tiff", "jpg", "bmp", "jpeg", "jpe"),
        "video" to listOf("mp4", "mpg", "mpeg", "avi", "webm", "mkv", "mpg4", "mov", "3gp"),
        "model" to listOf("stl", "obj", "3mf"),
        "sliced" to listOf("gcode", "gco", "pwmo", "pwmx"),
    )

    val modelFileType = listOf(
        ModelFileType.image to listOf(
            "png", "tif", "tiff", "jpg", "bmp", "jpeg", "jpe", "mp4", "mpg",
            "mpeg", "avi", "webm", "mkv", "mpg4", "mov", "3gp",
        ),
        ModelFileType.diagram to listOf("eps", "svg", "odg"),
        ModelFileType.document to listOf("pdf", "txt", "docx", "xlsx", "doc", "xls", "ods", "odt"),
        ModelFileType.model to listOf("stl", "obj", "3mf"),
        ModelFileType.sliced to listOf("gcode", "gco", "pwmo", "pwmx"),
    )

    @Suppress("ConstPropertyName")
    const val unknownMimeType = "application/octet-stream"

    fun getMimeTypeFromFilename(filename: String): String {
        return getMimeType(getFileExtension(filename))
    }

    fun getMimeType(extension: String): String {
        return if (types.containsKey(extension)) {
            types[extension]!!
        } else {
            unknownMimeType
        }
    }

    fun getFileExtension(filename: String): String {
        return filename.substringAfterLast('.', "").lowercase()
    }

    fun getFilenameWithoutExtension(filename: String): String {
        return filename.substringBeforeLast('.', "")
    }

    fun getApplicationFromFilename(filename: String): String? {
        return getApplication(getFileExtension(filename))
    }

    fun getApplication(extension: String): String? {
        applications.forEach { (key, value) -> if (value.contains(extension)) return key }

        return null
    }

    fun getModelFileTypeFromFilename(filename: String): ModelFileType? {
        return getModelFileType(getFileExtension(filename))
    }

    fun getModelFileType(extension: String): ModelFileType? {
        return modelFileType.firstOrNull { (_, value) -> value.contains(extension) }?.first
    }

    fun hasExtension(filename: String): Boolean {
        return filename.contains(".") &&
            filename.split(".").last().length <= 4
    }

    fun imagineExtension(filename: String, imaginedExt: String): String {
        return if (hasExtension(filename)) filename else "$filename.$imaginedExt"
    }
}
