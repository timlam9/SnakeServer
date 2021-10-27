package com.beatsnake.data.database

import com.beatsnake.data.models.User
import com.beatsnake.data.models.UserRaw
import org.litote.kmongo.eq

class UsersRepository(private val mongoDB: Database) {

    suspend fun addUserToDatabase(name: String, email: String, password: String): Boolean {
        val userRawExists: Boolean = mongoDB.getUsersCollection().findOne(UserRaw::email eq email) != null
        val userRaw = UserRaw(
            email = email,
            name = name,
            password = password,
            highscore = 0
        )
        return when {
            userRawExists -> false
            else -> mongoDB.getUsersCollection().insertOne(userRaw).wasAcknowledged()
        }
    }

    suspend fun getUser(email: String): UserRaw? = mongoDB.getUsersCollection().findOne(UserRaw::email eq email)

    suspend fun deleteUser(email: String): Boolean = mongoDB.getUsersCollection().deleteOneById(email).wasAcknowledged()

    suspend fun updateUser(email: String, highscore: Int): Boolean = getUser(email)?.let { user ->
        mongoDB.getUsersCollection().updateOne(
            filter = UserRaw::email eq user.email,
            target = user.copy(highscore = highscore)
        ).wasAcknowledged()
    } ?: false

    suspend fun getAllUsers(): List<User> = mongoDB.getUsersCollection().find().toList().map { it.toUser() }

}