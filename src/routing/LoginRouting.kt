package com.beatsnake.routing

import com.beatsnake.data.auth.JwtManager
import com.beatsnake.data.database.UsersRepository
import com.beatsnake.data.models.RefreshTokenCredentials
import com.beatsnake.data.models.RegistrationCredentials
import com.beatsnake.domain.*
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.registerRouting(usersRepository: UsersRepository, jwtManager: JwtManager = JwtManager()) {
    routing {
        post(POST_USER) {
            with(call.receive<RegistrationCredentials>()) {
                val token = when(usersRepository.addUserToDatabase(name, email, password)) {
                    true -> jwtManager.generateToken(name, email)
                    false -> EMPTY
                }

                call.respond(RegisteredUser(name, email, token))
            }
        }
        post(REFRESH_TOKEN) {
            with(call.receive<RefreshTokenCredentials>()) {
                val token = when(usersRepository.userExists(email)) {
                    true -> jwtManager.generateToken(name, email)
                    false -> EMPTY
                }

                call.respond(token)
            }
        }
    }
}
