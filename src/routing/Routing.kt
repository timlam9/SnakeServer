package com.beatsnake.routing

import com.beatsnake.data.database.UsersRepository
import com.beatsnake.data.models.User
import com.beatsnake.domain.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.userRoutes(repository: UsersRepository) {
    routing {
        getAllUsers(repository)
        userRoute(repository)
    }
}

private fun Routing.userRoute(repository: UsersRepository) {
    authenticate(JWT_AUTH) {
        route(USER) {
            getUser(repository)
            deleteUser(repository)
            updateUser(repository)
        }
    }
}

private fun Route.updateUser(repository: UsersRepository) {
    post {
        with(call.receive() as User) {
            val response = when (repository.updateUser(email, highscore)) {
                true -> email
                false -> NO_USER_FOUND
            }
            call.respond(response)
        }
    }
}

private fun Route.getUser(repository: UsersRepository) {
    get {
        val email = call.request.queryParameters[EMAIL] ?: EMPTY
        val response = repository.getUser(email) ?: NO_USER_FOUND
        call.respond(response)
    }
}

private fun Route.deleteUser(repository: UsersRepository) {
    delete {
        val email = call.request.queryParameters[EMAIL] ?: EMPTY
        val isSuccess = repository.deleteUser(email)
        call.respond(isSuccess)
    }
}

private fun Routing.getAllUsers(repository: UsersRepository) {
    authenticate(JWT_AUTH) {
        get(USERS) {
            call.respond(repository.getAllUsers())
        }
    }
}