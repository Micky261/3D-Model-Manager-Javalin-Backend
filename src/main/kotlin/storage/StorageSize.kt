package storage

import com.google.inject.Guice
import com.google.inject.Injector
import core.BackendModule
import core.config.bean.storage.AppConfigStorage
import data.dao.ModelFileDao
import dev.misfitlabs.kotlinguice4.getInstance

object StorageSize {
    private val injector: Injector = Guice.createInjector(BackendModule())
    private val modelFileDao = injector.getInstance<ModelFileDao>()

    private val capacityRegex = Regex("(\\d+)([BKMGT])?", RegexOption.IGNORE_CASE)

    fun fitsInStorage(fileSize: Long, storage: AppConfigStorage): Boolean {
        return getFreeSizeOfStorageInByte(storage) >= fileSize
    }

    fun getFreeSizeOfStorageInByte(storage: AppConfigStorage): Long {
        return getSizeOfStorageInByte(storage) - getOccupiedSizeOfStorageInByte(storage)
    }

    fun getSizeOfStorageInByte(storage: AppConfigStorage): Long {
        val sizeList = capacityRegex.find(storage.capacity)?.groupValues ?: listOf(storage.capacity, storage.capacity)

        return when (sizeList.size) {
            2 -> sizeList[1].toLong() // only first group matched (no unit)
            3 -> sizeList[1].toLong() * unitToMagnitude(sizeList[2]) // first and second group matched
            else -> 0 // unknown case
        }
    }

    fun getOccupiedSizeOfStorageInByte(storage: AppConfigStorage): Long {
        return modelFileDao.getStorageSize(storage.name)
    }

    fun unitToMagnitude(unit: String): Long {
        return when (unit.uppercase()) {
            "K" -> 1000L
            "M" -> 1000000L
            "G" -> 1000000000L
            "T" -> 1000000000000L
            else -> 1
        }
    }
}
