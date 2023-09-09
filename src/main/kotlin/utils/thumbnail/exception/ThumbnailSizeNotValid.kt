package utils.thumbnail.exception

import utils.thumbnail.ThumbnailFormat

class ThumbnailSizeNotValid(format: ThumbnailFormat, size: Int) :
    IllegalArgumentException("Thumbnail format ${format.name} with size $size is not supported.")
