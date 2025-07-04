package data.bean

@Suppress("EnumEntryName")
enum class ModelFileType {
    model,
    image,
    diagram,
    document,
    sliced,
    various,
    automatic, // Special type for upload
    all, // Special type for zip download
}
