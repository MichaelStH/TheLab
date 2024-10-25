package com.riders.thelab.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.riders.thelab.core.data.local.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllUsers(users: List<User>)

    @Query("SELECT * FROM user")
    fun getUsers(): Flow<List<User>>

    @Query("SELECT * FROM user")
    fun getUsersSync(): List<User>

    @Query("SELECT * FROM user WHERE id LIKE :userId")
    fun getUserByID(userId: Int): User

    @Query("SELECT * FROM user WHERE last_name LIKE :lastname")
    fun getUserByName(lastname: String): User

    @Query("SELECT * FROM user WHERE username LIKE :username")
    fun getUserByUsername(username: String): User

    @Query("SELECT * FROM user WHERE email LIKE :email")
    fun getUserByEmail(email: String): User

    @Query("SELECT * FROM user WHERE email LIKE :email AND isGoogleAuth LIKE 1")
    fun getUserWithGoogle(email: String): User?

    @Query("UPDATE user SET logged = :logged WHERE id LIKE :userId")
    fun setUserLogged(userId: Int, logged: Boolean = true)

    @Query("SELECT * FROM user WHERE username LIKE :username AND  password LIKE :password ")
    fun logInWithUsername(username: String, password: String): User?

    @Query("SELECT * FROM user WHERE email LIKE :email AND  password LIKE :password ")
    fun logInWithEmail(email: String, password: String): User?

    @Query("UPDATE user SET logged = :logOut WHERE id LIKE :userId")
    fun logoutUser(userId: Int, logOut: Boolean = false)

    @Query("DELETE FROM user WHERE id LIKE :userId")
    fun deleteUser(userId: Int)
}