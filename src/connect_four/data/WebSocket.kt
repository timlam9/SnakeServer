package com.beatsnake.connect_four.data

import com.beatsnake.connect_four.data.SocketMessage.*
import com.beatsnake.connect_four.data.SocketMessage.InBound.*
import com.beatsnake.connect_four.domain.ScoreFourEngine
import com.beatsnake.domain.JWT_AUTH
import com.beatsnake.domain.SCORE_FOUR
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json

fun Application.scoreFourRoute(scoreFourEngine: ScoreFourEngine) {
    routing { authenticate(JWT_AUTH) { webSocket(SCORE_FOUR) { responseToClient(scoreFourEngine) } } }
}

private suspend fun DefaultWebSocketServerSession.responseToClient(scoreFourEngine: ScoreFourEngine) {
    for (frame in incoming) {
        when (frame) {
            is Frame.Text -> {
                val receivedText = frame.readText()
                when (val message = Json.decodeFromString(InBound.serializer(), receivedText)) {
                    is Connect -> connectPlayer(scoreFourEngine,message.email)
                    is Move -> scoreFourEngine.handleMove(message.email, message.move)
                    is Disconnect -> scoreFourEngine.sendDisconnectionMessageAndDestroyGame(message)
                }
            }
            else -> Unit
        }
    }
}

private suspend fun DefaultWebSocketServerSession.connectPlayer(scoreFourEngine: ScoreFourEngine, email: String) {
    scoreFourEngine.createConnection(this, email)
    scoreFourEngine.createNewGameIfPossible()
}
