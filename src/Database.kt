package com.beatsnake

import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.io.FileInputStream
import java.util.*

class Database {

    companion object {

        const val BEAT_SNAKE_DB = "beat_snake_db"
        const val USERS_COLLECTION = "users"
    }

    private val connectionString: String = "mongodb+srv://snaker:snake030721@cluster030721.pownn.mongodb.net/beat_snake_db?retryWrites=true&w=majority"
    private val client = KMongo.createClient(connectionString).coroutine
    private val database = client.getDatabase(BEAT_SNAKE_DB)
    private val usersCollection: CoroutineCollection<User> = database.getCollection(USERS_COLLECTION)

    fun getUsersCollection(): CoroutineCollection<User> = usersCollection

    private fun getConnectionString(): String {
        val fis = FileInputStream("local.properties")
        val prop = Properties()
        prop.load(fis)

        return prop.getProperty("mongo_connection_string")
    }

}
