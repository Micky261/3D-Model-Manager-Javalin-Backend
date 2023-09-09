package utils.thumbnail

enum class ThumbnailFormat(val availableSizes: List<Int>, val postFixFormat: String) {
    Rectangular(listOf(200, 450, 700), "_R%d"),
    Quadratic(listOf(200), "_Q%d"),
}
