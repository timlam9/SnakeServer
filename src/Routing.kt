package com.beatsnake

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.coroutine.CoroutineCollection

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()

fun Application.userRoutes() {
    val mongoDB = Database()
    val usersCollection = mongoDB.getUsersCollection()

    routing {
        install(StatusPages) {
            exception<AuthenticationException> { cause ->
                call.respond(HttpStatusCode.Unauthorized)
            }
            exception<AuthorizationException> { cause ->
                call.respond(HttpStatusCode.Forbidden)
            }

        }

        getAllUsers(collection = usersCollection)
        userRoute(collection = usersCollection)
    }
}

private fun Routing.userRoute(collection: CoroutineCollection<User>) {
    route("/user") {
        createUser(collection = collection)
        getUser(collection = collection)
        deleteUser(collection = collection)
    }
}

private fun Route.createUser(collection: CoroutineCollection<User>) {
    post {
        val requestBody = call.receive<User>()
        val isSuccess = collection.insertOne(requestBody).wasAcknowledged()
        call.respond(isSuccess)
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