package data.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.inject.Inject
import data.bean.AppRight
import data.bean.User
import data.dao.ModelFileDao
import data.dao.UserDao
import data.dao.UserDaoClass
import data.dto.UserAdminDto
import storage.Storage
import utils.thumbnail.ThumbnailService

class UserService @Inject constructor(
    private val userDao: UserDao,
    private val userDaoClass: UserDaoClass,
    private val modelFileDao: ModelFileDao,
) {
    fun insert(name: String, email: String, plainPassword: String): Long {
        val hashedPassword = hashPassword(plainPassword)
        return userDao.insert(name, email, hashedPassword)
    }

    fun get(email: String): User? = userDao.getUserByMail(email)

    fun getById(id: Long): User? = userDao.getUserById(id)

    fun emailExists(email: String): Boolean = get(email) != null

    fun checkLogin(email: String, password: String): User? {
        val user = get(email) ?: return null

        return if (verifyPassword(password, user.password)) user else null
    }

    fun hashPassword(plainPassword: String): String = BCrypt.withDefaults().hashToString(
        14,
        plainPassword.toCharArray(),
    )

    fun verifyPassword(plainPassword: String, hashPassword: String): Boolean = BCrypt.verifyer().verify(
        plainPassword.toCharArray(),
        hashPassword,
    ).verified

    fun verifyPasswordForUser(userId: Long, plainPassword: String): Boolean {
        val user = getById(userId) ?: return false
        return verifyPassword(plainPassword, user.password)
    }

    fun changePassword(userId: Long, newPlainPassword: String) {
        val hashedPassword = hashPassword(newPlainPassword)
        userDao.updatePassword(userId, hashedPassword)
    }

    fun changeName(userId: Long, name: String) {
        userDao.updateName(userId, name)
    }

    fun setPendingEmail(userId: Long, email: String) {
        userDao.setPendingEmail(userId, email)
    }

    fun applyPendingEmail(userId: Long) {
        userDao.applyPendingEmail(userId)
    }

    fun clearPendingEmail(userId: Long) {
        userDao.clearPendingEmail(userId)
    }

    fun hasAnyUsers(): Boolean = userDao.count() > 0

    fun makeAdmin(userId: Long) {
        userDao.updateRights(userId, AppRight.Admin.name)
    }

    fun isAdmin(userId: Long): Boolean {
        val user = getById(userId) ?: return false
        return user.isAdmin
    }

    fun changeEmailAdmin(userId: Long, newEmail: String) {
        userDao.updateEmail(userId, newEmail)
    }

    fun getAllWithStats(): List<UserAdminDto> = userDaoClass.getAllWithStats()

    fun deleteUser(userId: Long) {
        val files = modelFileDao.getFilesByUser(userId)
        files.forEach { file ->
            val storage = Storage.getStorageClassByName(file.storage)
            storage.deleteFile(storage.getUserFilePath(file.userId, file.modelId, file.type, file.filename))
            ThumbnailService.deleteThumbnails(file.userId, file.modelId, file.filename)
        }
        userDao.delete(userId)
    }
}
