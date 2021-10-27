package com.beatsnake.routing

import com.beatsnake.data.auth.AuthToken
import com.beatsnake.data.auth.JwtManager
import com.beatsnake.data.database.UsersRepository
import com.beatsnake.data.models.RefreshTokenCredentials
import com.beatsnake.data.models.RegistrationCredentials
import com.beatsnake.domain.*
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.registerRouting(usersRepository: UsersRepository, jwtManager: JwtManager) {
    routing {
        post(POST_USER) {
            with(call.receive<RegistrationCredentials>()) {
                val token: String = when(usersRepository.addUserToDatabase(name, email, password)) {
                    true -> jwtManager.generateToken(name, email)
                    false -> EMPTY
                }

                call.respond(RegisteredUser(name, email, token))
            }
        }
        post(REFRESH_TOKEN) {
            with(call.receive<RefreshTokenCredentials>()) {
                val token: String = when(usersRepository.getUser(email)) {
                    null -> EMPTY
                    else -> jwtManager.generateToken(name, email)
                }

                call.respond(AuthToken(token))
            }
        }
    }
}
