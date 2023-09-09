package storage

import com.google.inject.Guice
import com.google.inject.Injector
import core.BackendModule
import core.config.AppConfig
import core.config.bean.storage.AppConfigLocalStorage
import core.config.bean.storage.AppConfigStorage
import core.config.bean.storage.AppConfigWebDavStorage
import core.config.bean.storage.StorageType
import dev.misfitlabs.kotlinguice4.getInstance
import storage.bean.AbstractStorage
import storage.bean.LocalStorage
import storage.bean.WebDavStorage
import storage.exception.NoDefaultStorageException

object Storage {
    private val injector: Injector = Guice.createInjector(BackendModule())
    private val config = injector.getInstance<AppConfig>()

    @Suppress("ConstPropertyName")
    const val temporaryStorageBasePath = "./upload_temp"

    fun getRandomStorageClass(fileSizeToStore: Long): AbstractStorage {
        val fittingStorages = config.config.storage.filter { StorageSize.fitsInStorage(fileSizeToStore, it) }

        if (fittingStorages.isNotEmpty()) {
            return getStorageClass(fittingStorages.random())
        } else {
            throw Exception("No storages available which can save the given file.")
        }
    }

    fun getStorageClass(storage: AppConfigStorage): AbstractStorage {
        return when (storage.storageType) {
            StorageType.Local -> LocalStorage(storage as AppConfigLocalStorage)
            StorageType.WebDav -> WebDavStorage(storage as AppConfigWebDavStorage)
        }
    }

    fun getStorageClassByName(storageName: String): AbstractStorage {
        return getStorageClass(getStorageByName(storageName))
    }

    fun getStorageByName(storageName: String): AppConfigStorage {
        return config.config.storage.first { it.name == storageName }
    }

    fun getDefaultStorage(): AbstractStorage {
        val defaultStorage = config.config.storage.firstOrNull { it.default } ?: throw NoDefaultStorageException()
        return getStorageClass(defaultStorage)
    }
}
