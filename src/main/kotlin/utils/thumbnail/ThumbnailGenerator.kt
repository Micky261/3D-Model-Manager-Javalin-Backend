package utils.thumbnail

import storage.Storage
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

class ThumbnailGenerator(
    val userId: Long,
    val modelId: Long,
    val file: InputStream,
    val filename: String,
) {
    private var img: BufferedImage
    private var copy: ByteArrayOutputStream = ByteArrayOutputStream()

    init {
        file.copyTo(copy)
        img = ImageIO.read(ByteArrayInputStream(copy.toByteArray()))
    }

    fun generateThumbnailSet() {
        ThumbnailFormat.Rectangular.availableSizes.forEach { generateThumbnailRectangular(it) }

        ThumbnailFormat.Quadratic.availableSizes.forEach { generateThumbnailQuadratic(it) }

        copy.close()
    }

    fun generateThumbnailQuadratic(dimension: Int) {
        quadraticThumb(img, dimension)
    }

    fun generateThumbnailRectangular(height: Int) {
        if (img.height > height) {
            resizeImage(
                img,
                targetHeight = height,
                targetWidth = (img.width.toDouble() * (height.toDouble() / img.height.toDouble())).toInt(),
            )
        } else {
            val storage = Storage.getDefaultStorage()
            storage.uploadFile(
                ByteArrayInputStream(copy.toByteArray()),
                storage.getThumbnailPath(userId, modelId),
                ThumbnailService.getFilename(filename, ThumbnailFormat.Rectangular, height),
            )
        }
    }

    private fun resizeImage(originalImage: BufferedImage, targetHeight: Int, targetWidth: Int) {
        try {
            val resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH)
            val outputImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
            outputImage.graphics.drawImage(resultingImage, 0, 0, null)

            val os = ByteArrayOutputStream()
            ImageIO.write(outputImage, "jpg", os)
            val inpStr: InputStream = ByteArrayInputStream(os.toByteArray())

            val storage = Storage.getDefaultStorage()
            storage.uploadFile(
                inpStr,
                storage.getThumbnailPath(userId, modelId),
                ThumbnailService.getFilename(filename, ThumbnailFormat.Rectangular, targetHeight),
            )

            os.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("DuplicatedCode")
    private fun quadraticThumb(o: BufferedImage, targetDimension: Int) {
        val quadratic = if (o.width > o.height) {
            val targetWidth = (o.width.toDouble() * (targetDimension.toDouble() / o.height.toDouble())).toInt()
            val resultingImage = o.getScaledInstance(targetWidth, targetDimension, Image.SCALE_SMOOTH)
            val outputImage = BufferedImage(targetWidth, targetDimension, BufferedImage.TYPE_INT_RGB)
            outputImage.graphics.drawImage(resultingImage, 0, 0, null)

            outputImage.getSubimage((outputImage.width - targetDimension) / 2, 0, targetDimension, targetDimension)
        } else {
            val targetHeight = (o.height.toDouble() * (targetDimension.toDouble() / o.width.toDouble())).toInt()
            val resultingImage = o.getScaledInstance(targetDimension, targetHeight, Image.SCALE_SMOOTH)
            val outputImage = BufferedImage(targetDimension, targetHeight, BufferedImage.TYPE_INT_RGB)
            outputImage.graphics.drawImage(resultingImage, 0, 0, null)

            outputImage.getSubimage(0, (outputImage.height - targetDimension) / 2, targetDimension, targetDimension)
        }

        val os = ByteArrayOutputStream()
        ImageIO.write(quadratic, "jpg", os)
        val inpStr: InputStream = ByteArrayInputStream(os.toByteArray())

        val storage = Storage.getDefaultStorage()
        storage.uploadFile(
            inpStr,
            storage.getThumbnailPath(userId, modelId),
            ThumbnailService.getFilename(filename, ThumbnailFormat.Quadratic, targetDimension),
        )

        os.close()
    }
}
