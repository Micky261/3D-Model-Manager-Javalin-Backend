package data.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.inject.Inject
import data.bean.User
import data.dao.UserDao

class UserService @Inject constructor(
    private val userDao: UserDao,
) {
//    fun insert(user: User) {
//        userDao.insert(user)
//    }

    fun get(email: String): User? {
        return userDao.getUserByMail(email)
    }

    fun checkLogin(email: String, password: String): User? {
        val user = get(email) ?: return null

        return if (verifyPassword(password, user.password)) user else null
    }

    fun hashPassword(plainPassword: String): String {
        return BCrypt.withDefaults().hashToString(14, plainPassword.toCharArray())
    }

    private fun verifyPassword(plainPassword: String, hashPassword: String): Boolean {
        return BCrypt.verifyer().verify(plainPassword.toCharArray(), hashPassword).verified
    }
}
