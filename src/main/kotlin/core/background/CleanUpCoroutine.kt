package core.background

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object CleanUpCoroutine {
    private val cleanupPackages: List<CleanupPackage> = listOf(
        CleanupPackage(cleanTemp(), 1.hours),
        CleanupPackage(cleanUploadTemp(), 30.minutes),
    )

    @OptIn(DelicateCoroutinesApi::class)
    fun start() {
        GlobalScope.launch {
            while (true) {
                try {
                    cleanupPackages.forEach { cp ->
                        if (cp.lastRun.plusSeconds(cp.interval.inWholeSeconds).isAfter(Instant.now())) {
                            System.gc()
                            cp.runFunction
                            System.gc()
                            cp.lastRun = Instant.now()
                        }
                    }
                } catch (e: Exception) {
                    println(e.message)
                }

                delay(5.minutes)
            }
        }
    }

    private fun cleanTemp() {
        File("temp").listFiles()?.forEach { file ->
            val time = file.nameWithoutExtension.split("-").last().toLong()

            if (Instant.now().isAfter(Instant.ofEpochSecond(time).plusSeconds(5.hours.inWholeSeconds))) {
                file.delete()
            }
        }
    }

    private fun cleanUploadTemp() {
        // All files in upload_temp/chunked directory
        File("upload_temp/chunked").listFiles()?.filter { it.isDirectory }?.forEach { chunkedDir ->
            // All files in upload_temp/chunked/{userId}
            chunkedDir.listFiles()?.filter { it.isDirectory }?.forEach { userDir ->
                val time = userDir.name.toLong()

                if (Instant.now().isAfter(Instant.ofEpochMilli(time).plusSeconds(2.hours.inWholeSeconds))) {
                    userDir.deleteRecursively()
                }
            }

            if (chunkedDir.listFiles()?.isEmpty() == true) {
                chunkedDir.delete()
            }
        }
    }

    private data class CleanupPackage(
        val runFunction: Unit,
        val interval: Duration,
        var lastRun: Instant = Instant.now(),
    )
}
