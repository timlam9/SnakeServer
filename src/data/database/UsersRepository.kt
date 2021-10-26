package com.beatsnake.data.database

import com.beatsnake.data.models.User
import org.litote.kmongo.eq

class UsersRepository(private val mongoDB: Database = Database()) {

    suspend fun addUserToDatabase(name: String, email: String, password: String): Boolean {
        val userExists: Boolean = mongoDB.getUsersCollection().findOne(User::email eq email) != null
        val user = User(
            email = email,
            name = name,
            password = password,
            highscore = 0
        )
        return when {
            userExists -> false
            else -> mongoDB.getUsersCollection().insertOne(user).wasAcknowledged()
        }
    }

    suspend fun getUser(email: String): User? = mongoDB.getUsersCollection().findOne(User::email eq email)

    suspend fun userExists(email: String): Boolean = getUser(email) != null

    suspend fun deleteUser(email: String): Boolean = mongoDB.getUsersCollection().deleteOneById(email).wasAcknowledged()

    suspend fun updateUser(email: String, highscore: Int): Boolean = getUser(email)?.let { user ->
        mongoDB.getUsersCollection().updateOne(
            filter = User::email eq user.email,
            target = user.copy(highscore = highscore)
        ).wasAcknowledged()
    } ?: false

    suspend fun getAllUsers() = mongoDB.getUsersCollection().find().toList()

}