package com.beatsnake.routing

import com.beatsnake.connect_four.*
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.util.*

private val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
private var games: MutableList<Game> = mutableListOf()
private var creatingGame = false

@ExperimentalSerializationApi
fun Application.scoreFourRoute() {
    routing {
        webSocket("/scoreFour") {
            connectPlayers()
            logConnections()
            responseToClient()
        }
    }
}

private suspend fun DefaultWebSocketServerSession.connectPlayers() {
    val thisConnection = Connection(this, UUID.randomUUID().toString())
    connections += thisConnection

    if (connections.size >= 2 && !creatingGame) createNewGame()
}

private fun Application.logConnections() {
    log.info("========== ========== ========== ========== ==========")
    log.info("$connections")
    log.info(" Connections size: ${connections.size}")
    log.info("\n")
}

@ExperimentalSerializationApi
private suspend fun DefaultWebSocketServerSession.responseToClient() {
    for (frame in incoming) {
        when (frame) {
            is Frame.Text -> {
                val receivedText = frame.readText()
                when (val message = Json.decodeFromString(SocketMessage.serializer(), receivedText)) {
                    is SocketMessage.Move -> handleMove(message.userID, message.move)
                    is SocketMessage.GameOver -> Unit
                }
            }
            else -> Unit
        }
    }
}

private suspend fun handleMove(userID: String, move: Int) {
    var game = findGameByPlayerID(userID) ?: return

    val turn = when (userID) {
        game.player.id -> Turn.Player
        game.opponent.id -> Turn.Opponent
        else -> Turn.Opponent
    }

    game = game.copy(board = game.board.update(updatedSlot = move, turn = turn))
    games.updateGame(game)

    val playerResponse = game.board.updateGameStatus(turn.next())
    val opponentResponse = game.board.updateGameStatus(turn)

    game.player.session.send(playerResponse.toJson())
    game.opponent.session.send(opponentResponse.toJson())

    removeGameWhenGameIsOver(playerResponse, opponentResponse, game)
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
        player.session.send(encodeInitialResponse(Turn.Player, player.id))
        opponent.session.send(encodeInitialResponse(Turn.Opponent, opponent.id))
    }

    creatingGame = false
}


private fun Game.encodeInitialResponse(turn: Turn, playerID: String) = SocketMessage.StartTurn(
    board = board,
    turn = turn,
    userID = playerID
).toJson()

private fun MutableSet<Connection>.getAndRemoveFirst(): Connection = first().apply { remove(first()) }

private fun MutableList<Game>.updateGame(game: Game) {
    remove(findGameByGameID(game.gameID))
    add(game)
}

private fun findGameByPlayerID(userID: String): Game? = games.find { it.player.id == userID || it.opponent.id == userID }

private fun findGameByGameID(gameID: String): Game? = games.find { it.gameID == gameID }