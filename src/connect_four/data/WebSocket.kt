package com.beatsnake.connect_four.data

import com.beatsnake.connect_four.data.SocketMessage.*
import com.beatsnake.connect_four.data.SocketMessage.InBound.*
import com.beatsnake.connect_four.domain.GameResponse
import com.beatsnake.connect_four.domain.ScoreFourEngine
import com.beatsnake.connect_four.domain.models.Game
import com.beatsnake.domain.JWT_AUTH
import com.beatsnake.domain.SCORE_FOUR
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json

fun Application.scoreFourRoute(scoreFourEngine: ScoreFourEngine, lobby: Lobby) {
    routing {
        authenticate(JWT_AUTH) {
            webSocket(SCORE_FOUR) {
                responseToClient(scoreFourEngine, lobby)
            }
        }
    }
}

private suspend fun DefaultWebSocketServerSession.responseToClient(
    scoreFourEngine: ScoreFourEngine,
    lobby: Lobby
) {
    for (frame in incoming) {
        when (frame) {
            is Frame.Text -> {
                val receivedText = frame.readText()
                when (val message = Json.decodeFromString(InBound.serializer(), receivedText)) {
                    is Connect -> lobby.createConnection(this, message.email)
                    is Disconnect -> lobby.sendDisconnectionMessageAndDestroyGame(message)
                    is Move -> handleMove(lobby, message, scoreFourEngine)
                }
            }
            else -> Unit
        }
    }
}

private suspend fun handleMove(
    lobby: Lobby,
    message: Move,
    scoreFourEngine: ScoreFourEngine
) {
    val preMoveGame = lobby.findGameByPlayerEmail(message.email) ?: return

    when (val updatedGame = scoreFourEngine.handleMove(preMoveGame, message.email, message.move)) {
        is GameResponse.Error -> sendErrorResponse(preMoveGame, message)
        is GameResponse.UpdatedGame -> {
            with(updatedGame) {
                lobby.updateGame(game)
                sendUpdateGameResponse(this)
                lobby.removeGameWhenGameIsOver(
                    game = game,
                    playerResponse = playerResponse,
                    opponentResponse = opponentResponse
                )
            }
        }
    }
}

private suspend fun sendUpdateGameResponse(updatedGame: GameResponse.UpdatedGame) {
    updatedGame.game.player.session.send(updatedGame.playerResponse.toJson())
    updatedGame.game.opponent.session.send(updatedGame.opponentResponse.toJson())
}

private suspend fun sendErrorResponse(
    game: Game,
    message: Move
) {
    val error = OutBound.SocketError("ColumnAlreadyFilled").toJson()

    when (message.email) {
        game.player.email -> game.player.session.send(error)
        game.opponent.email -> game.opponent.session.send(error)
    }
}
