package data.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.inject.Inject
import data.bean.AppRight
import data.bean.User
import data.dao.UserDao

class UserService @Inject constructor(
    private val userDao: UserDao,
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

    private fun verifyPassword(plainPassword: String, hashPassword: String): Boolean = BCrypt.verifyer().verify(
        plainPassword.toCharArray(),
        hashPassword,
    ).verified

    fun hasAnyUsers(): Boolean = userDao.count() > 0

    fun makeAdmin(userId: Long) {
        userDao.updateRights(userId, AppRight.Admin.name)
    }

    fun isAdmin(userId: Long): Boolean {
        val user = getById(userId) ?: return false
        return user.isAdmin
    }
}
