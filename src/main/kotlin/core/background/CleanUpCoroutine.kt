package core.background

import com.google.inject.Injector
import data.dao.EmailVerificationDao
import data.dao.UserDao
import data.services.EmailVerificationService
import dev.misfitlabs.kotlinguice4.getInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.Logger
import java.io.File
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object CleanUpCoroutine {
    private lateinit var injector: Injector
    private lateinit var logger: Logger

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val cleanupPackages: MutableList<CleanupPackage> = mutableListOf()

    fun start(injector: Injector) {
        this.injector = injector
        this.logger = injector.getInstance()

        cleanupPackages.addAll(
            listOf(
                CleanupPackage(::cleanTemp, 1.hours),
                CleanupPackage(::cleanUploadTemp, 30.minutes),
                CleanupPackage(::cleanExpiredEmailVerifications, 6.hours),
            ),
        )

        scope.launch {
            while (true) {
                try {
                    cleanupPackages.forEach { cp ->
                        if (Instant.now().isAfter(cp.lastRun.plusSeconds(cp.interval.inWholeSeconds))) {
                            System.gc()
                            cp.runFunction()
                            System.gc()
                            cp.lastRun = Instant.now()
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Error during cleanup", e)
                }

                delay(5.minutes)
            }
        }
    }

    fun stop() {
        logger.info("Stopping cleanup coroutine...")
        scope.cancel()
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

    private fun cleanExpiredEmailVerifications() {
        val emailVerificationDao = injector.getInstance<EmailVerificationDao>()
        val userDao = injector.getInstance<UserDao>()

        // Get user IDs with expired verifications
        val expiryTime = Instant.now().minusSeconds(EmailVerificationService.TOKEN_VALIDITY_HOURS * 60 * 60).epochSecond
        val affectedUserIds = emailVerificationDao.getExpiredUserIds(expiryTime)

        // Delete expired verification tokens
        val deletedTokens = emailVerificationDao.deleteExpired(expiryTime)

        if (deletedTokens > 0) {
            logger.info("Cleaned up $deletedTokens expired email verification token(s)")
        }

        // Delete unverified users who no longer have any verification tokens
        var deletedUsers = 0
        affectedUserIds.distinct().forEach { userId ->
            // Check if user still has a valid verification token (e.g., they requested a new one)
            val hasValidToken = emailVerificationDao.getByUserId(userId) != null

            if (!hasValidToken) {
                // Delete user only if they are unverified and have no remaining tokens
                val deleted = userDao.deleteUnverifiedUser(userId)
                deletedUsers += deleted
            }
        }

        if (deletedUsers > 0) {
            logger.info("Cleaned up $deletedUsers unverified user account(s) with expired registration")
        }
    }

    private data class CleanupPackage(
        val runFunction: () -> Unit,
        val interval: Duration,
        var lastRun: Instant = Instant.now(),
    )
}
