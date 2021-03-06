package com.beatsnake.data.database

import com.beatsnake.data.models.UserRaw
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class Database {

    companion object {

        const val BEAT_SNAKE_DB = "beat_snake_db"
        const val USERS_COLLECTION = "users"
        const val MONGODB_URI = "MONGODB_URI"

    }

    private val connectionString: String = System.getenv(MONGODB_URI)
    private val client = KMongo.createClient(connectionString).coroutine
    private val database = client.getDatabase(BEAT_SNAKE_DB)
    private val usersCollection: CoroutineCollection<UserRaw> = database.getCollection(USERS_COLLECTION)

    fun getUsersCollection(): CoroutineCollection<UserRaw> = usersCollection

}
