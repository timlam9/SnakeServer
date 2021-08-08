package com.beatsnake

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.util.idValue

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()

fun Application.userRoutes() {
    val mongoDB = Database()
    val usersCollection = mongoDB.getUsersCollection()

    routing {
        install(StatusPages) {
            exception<AuthenticationException> { call.respond(HttpStatusCode.Unauthorized) }
            exception<AuthorizationException> { call.respond(HttpStatusCode.Forbidden) }
        }

        getAllUsers(collection = usersCollection)
        userRoute(collection = usersCollection)
    }
}

private fun Routing.userRoute(collection: CoroutineCollection<User>) {
    route("/user") {
        getUser(collection = collection)
        deleteUser(collection = collection)
        updateUser(collection = collection)
    }
}

private fun Route.updateUser(collection: CoroutineCollection<User>) {
    post {
        val user: User = call.receive()
        val userID: String = user.id

        val isSuccess = when {
            userID.isEmpty() -> collection.insertOne(user).wasAcknowledged()
            else -> collection.updateOne(user).wasAcknowledged()
        }
        val response = if (isSuccess) userID else ""

        call.respond(response)
    }
}

private fun Route.getUser(collection: CoroutineCollection<User>) {
    get {
        val id = call.request.queryParameters["id"] ?: "no_id"
        val response = collection.findOneById(id) ?: "User not found"
        call.respond(response)
    }
}

private fun Route.deleteUser(collection: CoroutineCollection<User>) {
    delete {
        val id = call.request.queryParameters["id"] ?: "no_id"
        val isSuccess = collection.deleteOneById(id).wasAcknowledged()
        call.respond(isSuccess)
    }
}

private fun Routing.getAllUsers(collection: CoroutineCollection<User>) {
    get("/users") {
        val users = collection.find().toList()
        call.respond(users)
    }
}