package com.beatsnake

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.beatsnake.connect_four.data.Lobby
import com.beatsnake.connect_four.data.scoreFourRoute
import com.beatsnake.connect_four.domain.ScoreFourEngine
import com.beatsnake.data.auth.JwtManager
import com.beatsnake.data.database.Database
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
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
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
            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim(USERNAME).asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

private fun Application.initRoutes() {
    val usersRepository = UsersRepository(Database())
    val jwtManager = JwtManager()
    val scoreFourEngine = ScoreFourEngine()
    val lobby = Lobby(this)

    registerRouting(usersRepository, jwtManager)
    userRoutes(usersRepository)
    scoreFourRoute(scoreFourEngine, lobby)
}



