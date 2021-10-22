package com.beatsnake

import com.beatsnake.connect_four.*
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.util.*

data class Connection(
    val session: DefaultWebSocketSession,
    val id: String = ""
)

private var board = Board.generateEmptyBoard()
private lateinit var player: Connection
private lateinit var opponent: Connection

@ExperimentalSerializationApi
fun Application.scoreFourRoute() {
    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())

        webSocket("/scoreFour") {
            connectPlayers(connections)
            responseToClient()
        }
    }
}

private suspend fun DefaultWebSocketServerSession.connectPlayers(connections: MutableSet<Connection>) {
    val thisConnection = Connection(this, UUID.randomUUID().toString())
    connections += thisConnection

    connectTwoPlayers(connections)
}

private suspend fun connectTwoPlayers(connections: MutableSet<Connection>) {
    if (connections.size > 1) {
        player = connections.random()
        opponent = connections.random()

        while (opponent == player) {
            opponent = connections.random()
        }

        board = Board.generateEmptyBoard()
        val playerResponse = Json.encodeToString(
            serializer = SocketMessage.serializer(),
            value = SocketMessage.StartTurn(
                board = board,
                turn = Turn.Player,
                userID = player.id
            )
        )
        val opponentResponse = Json.encodeToString(
            serializer = SocketMessage.serializer(),
            value = SocketMessage.StartTurn(
                board = board,
                turn = Turn.Opponent,
                userID = opponent.id
            )
        )

        player.session.send(playerResponse)
        opponent.session.send(opponentResponse)
    }
}

@ExperimentalSerializationApi
private suspend fun DefaultWebSocketServerSession.responseToClient() {
    for (frame in incoming) {
        when (frame) {
            is Frame.Text -> {
                val receivedText = frame.readText()
                when (val message = Json.decodeFromString(SocketMessage.serializer(), receivedText)) {
                    is SocketMessage.Move -> handleMove(message.userID, message.move)
                    is SocketMessage.StartTurn -> Unit
                }
            }
            else -> Unit
        }
    }
}

private suspend fun handleMove(userID: String, move: Int) {
    val turn = when (userID) {
        player.id -> Turn.Player
        opponent.id -> Turn.Opponent
        else -> Turn.Opponent
    }

    board = board.update(updatedSlot = move, turn = turn)

    val response = board.updateGameStatus(turn.next())
    val opponentResponse = board.updateGameStatus(turn)

    player.session.send(response.toJson())
    opponent.session.send(opponentResponse.toJson())
}
