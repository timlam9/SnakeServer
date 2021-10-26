package com.beatsnake

import com.beatsnake.connect_four.data.scoreFourRoute
import com.beatsnake.data.auth.JwtManager.Companion.validateToken
import com.beatsnake.data.auth.JwtManager.Companion.verifyToken
import com.beatsnake.data.database.UsersRepository
import com.beatsnake.domain.*
import com.beatsnake.routing.registerRouting
import com.beatsnake.routing.userRoutes
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.serialization.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    installs()
    initRoutes()
}

private fun Application.installs() {
    val myRealm = environment.config.property(JWT_REALM).getString()

    install(ContentNegotiation) {
        gson()
        json()
    }
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(FIFTEEN_SECONDS)
        timeout = Duration.ofSeconds(FIFTEEN_SECONDS)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    install(StatusPages) {
        exception<AuthenticationException> { call.respond(HttpStatusCode.Unauthorized) }
        exception<AuthorizationException> { call.respond(HttpStatusCode.Forbidden) }
    }
    install(Authentication) {
        jwt(JWT_AUTH) {
            realm = myRealm
            verifyToken()
            validateToken()
        }
    }
}


private fun Application.initRoutes(usersRepository: UsersRepository = UsersRepository()) {
    registerRouting(usersRepository)
    userRoutes(usersRepository)
    scoreFourRoute()
    
    CoroutineScope(this.coroutineContext).launch {
        log.info("Users: ${usersRepository.getAllUsers()}")
    }

}



