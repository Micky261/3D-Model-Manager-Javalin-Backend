package utils

import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

/**
 * @author Frank Nelles
 * @since 14.08.2020
 * @param totalChunks Total number of chunks the file contains
 * @param path Path to the file (without filename and without trailing slash
 * @param fileName Filename
 */
class Chunking(
    private val totalChunks: Int,
    private val path: String,
    private val fileName: String,
) {
    companion object {
        /**
         * @param inputStream The InputStream to write into the file
         * @param path Path to the file (without filename and without trailing slash
         * @param fileName Filename
         * @param chunk The chunk number/id
         */
        fun streamToFileChunk(inputStream: InputStream, path: String, fileName: String, chunk: Int) {
            val chunkFile = File("$path/$fileName-chunks/$fileName.${chunkNamePostfix(chunk)}")
            chunkFile.parentFile.mkdirs()
            chunkFile.createNewFile()
            Files.copy(inputStream, chunkFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        /**
         * @param totalChunks Total number of chunks the file contains
         * @param path Path to the file (without filename and without trailing slash
         * @param fileName Filename
         */
        fun checkReady(totalChunks: Int, path: String, fileName: String): Boolean {
            val chunkPath = "$path/$fileName-chunks/"
            val filesPath = File(chunkPath).listFiles()!!.size

            return filesPath == totalChunks
        }

        val chunkNamePostfix = { num: Int -> "__chunk_$num" }
    }

    /**
     * Put chunks together
     */
    fun putTogether(): File {
        val targetFilePath = "$path/$fileName"
        val targetFileObject = File(targetFilePath)
        targetFileObject.createNewFile()
        val targetFile = Paths.get(targetFilePath)

        for (chunkNo in 0.rangeUntil(totalChunks)) {
            Files.write(
                targetFile,
                File("$path/$fileName-chunks/$fileName.${chunkNamePostfix(chunkNo)}").readBytes(),
                StandardOpenOption.APPEND,
            )
        }

        return targetFileObject
    }
}
