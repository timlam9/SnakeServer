package com.beatsnake.connect_four.data

import com.beatsnake.connect_four.data.SocketMessage.SocketError
import com.beatsnake.connect_four.domain.models.Game
import com.beatsnake.connect_four.domain.models.ColumnAlreadyFilledException
import com.beatsnake.connect_four.domain.models.Turn
import com.beatsnake.domain.JWT_AUTH
import com.beatsnake.domain.SCORE_FOUR
import connect_four.domain.models.Board
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import java.util.*

private val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
private var games: MutableList<Game> = mutableListOf()
private var creatingGame = false

fun Application.scoreFourRoute() {
    val application: Application = this
    routing { authenticate(JWT_AUTH) { webSocket(SCORE_FOUR) { responseToClient(application) } } }
}

private suspend fun DefaultWebSocketServerSession.connectPlayer(application: Application, email: String) {
    val thisConnection = Connection(this, email)
    connections += thisConnection
    application.logConnectionCreation()

    if (connections.size >= 2 && !creatingGame) createNewGame()
}

private fun Application.logConnectionCreation() {
    log.info("========== ========== ========== ========== ==========")
    log.info(" Connection created! ")
    log.info(" Connections size: ${connections.size}")
    log.info("========== ==========")
    log.info("\n")
}

private fun Application.logGameRemoval(gameID: String) {
    log.info("========== ========== ========== ========== ==========")
    log.info(" Game: $gameID removed!")
    log.info("========== ==========")
    log.info("\n")
}

private suspend fun DefaultWebSocketServerSession.responseToClient(application: Application) {
    for (frame in incoming) {
        when (frame) {
            is Frame.Text -> {
                val receivedText = frame.readText()
                when (val message = Json.decodeFromString(SocketMessage.serializer(), receivedText)) {
                    is SocketMessage.Connect -> connectPlayer(application, message.email)
                    is SocketMessage.Move -> handleMove(message.email, message.move)
                    is SocketMessage.GameOver -> Unit
                    is SocketMessage.Disconnect -> sendDisconnectionMessageAndDestroyGame(application, message)
                }
            }
            else -> Unit
        }
    }
}

private suspend fun sendDisconnectionMessageAndDestroyGame(
    application: Application,
    message: SocketMessage.Disconnect
) {
    val game = findGameByPlayerEmail(message.email) ?: return
    connections.removeIf { it.email == message.email }
    games.remove(game)
    application.logGameRemoval(game.gameID)

    val error = SocketError("ConnectionLost").toJson()
    when (game.player.email) {
        message.email -> game.opponent.session.send(error)
        else -> game.player.session.send(error)
    }
}

private suspend fun handleMove(email: String, move: Int) {
    var game = findGameByPlayerEmail(email) ?: return

    val turn = when (email) {
        game.player.email -> Turn.Player
        game.opponent.email -> Turn.Opponent
        else -> Turn.Opponent
    }

    try {
        game = game.copy(board = game.board.update(move, turn))
        games.updateGame(game)

        val playerResponse = game.board.updateGameStatus(turn.next())
        val opponentResponse = game.board.updateGameStatus(turn)

        game.player.session.send(playerResponse.toJson())
        game.opponent.session.send(opponentResponse.toJson())

        removeGameWhenGameIsOver(playerResponse, opponentResponse, game)
    } catch (e: ColumnAlreadyFilledException) {
        when (email) {
            game.player.email -> game.player.session.send(SocketError("ColumnAlreadyFilled").toJson())
            game.opponent.email -> game.opponent.session.send(SocketError("ColumnAlreadyFilled").toJson())
        }
    }

}

private fun removeGameWhenGameIsOver(
    playerResponse: SocketMessage,
    opponentResponse: SocketMessage,
    game: Game
) {
    if (
        playerResponse is SocketMessage.GameOver ||
        opponentResponse is SocketMessage.GameOver
    ) {
        games.remove(findGameByGameID(game.gameID))
    }
}

private suspend fun createNewGame() {
    creatingGame = true

    val game = Game(
        gameID = UUID.randomUUID().toString(),
        player = connections.getAndRemoveFirst(),
        opponent = connections.getAndRemoveFirst(),
        board = Board.generateEmptyBoard()
    )

    games.add(game)

    with(game) {
        player.session.send(encodeInitialResponse(Turn.Player))
        opponent.session.send(encodeInitialResponse(Turn.Opponent))
    }

    creatingGame = false
}


private fun Game.encodeInitialResponse(turn: Turn) = SocketMessage.StartTurn(
    board = board,
    turn = turn
).toJson()

private fun MutableSet<Connection>.getAndRemoveFirst(): Connection = first().apply { remove(first()) }

private fun MutableList<Game>.updateGame(game: Game) {
    remove(findGameByGameID(game.gameID))
    add(game)
}

private fun findGameByPlayerEmail(email: String) = games.find { it.player.email == email || it.opponent.email == email }

private fun findGameByGameID(gameID: String): Game? = games.find { it.gameID == gameID }