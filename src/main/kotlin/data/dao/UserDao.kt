package data.dao

import data.bean.User
import org.jdbi.v3.sqlobject.statement.SqlQuery

interface UserDao {
    @SqlQuery("SELECT * FROM users WHERE email = :email")
    fun getUserByMail(email: String): User?
}
