package com.beatsnake.data.database

import com.beatsnake.data.models.User
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class Database {

    companion object {

        const val BEAT_SNAKE_DB = "beat_snake_db"
        const val USERS_COLLECTION = "users"
        const val MONGODB_URI = "MONGODB_URI"

    }

    private val connectionString: String = "mongodb+srv://snaker:snake030721@cluster030721.pownn.mongodb.net/admin" //System.getenv(MONGODB_URI)
    private val client = KMongo.createClient(connectionString).coroutine
    private val database = client.getDatabase(BEAT_SNAKE_DB)
    private val usersCollection: CoroutineCollection<User> = database.getCollection(USERS_COLLECTION)

    fun getUsersCollection(): CoroutineCollection<User> = usersCollection

}
