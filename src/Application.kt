package com.beatsnake

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.serialization.*
import io.ktor.websocket.*
import kotlinx.serialization.ExperimentalSerializationApi
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@ExperimentalSerializationApi
@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        gson()
        json()
    }
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    install(StatusPages) {
        exception<AuthenticationException> { call.respond(HttpStatusCode.Unauthorized) }
        exception<AuthorizationException> { call.respond(HttpStatusCode.Forbidden) }
    }

//    userRoutes()
    scoreFourRoute()
}



