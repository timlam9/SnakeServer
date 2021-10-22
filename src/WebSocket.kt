package com.beatsnake

import com.beatsnake.connect_four.Availability
import com.beatsnake.connect_four.Board
import com.beatsnake.connect_four.Turn
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*

@Serializable
sealed class SocketMessage {

    @Serializable
    @SerialName("start_turn")
    data class StartTurn(val board: Board, val turn: Turn, val userID: String) : SocketMessage()

    @Serializable
    @SerialName("player_turn")
    data class PlayerTurn(val board: Board, val turn: Turn) : SocketMessage()

    @Serializable
    @SerialName("move")
    data class Move(val userID: String, val move: Int) : SocketMessage()

    @Serializable
    @SerialName("game_over")
    data class GameOver(val board: Board, val winner: GameOverStatus, val turn: Turn) : SocketMessage()

}

enum class GameOverStatus {
    Won,
    Draw
}

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
                    is SocketMessage.Move -> {
                        val turn = when (message.userID) {
                            player.id -> Turn.Player
                            opponent.id -> Turn.Opponent
                            else -> Turn.Opponent
                        }
                        board = board.update(updatedSlot = message.move, turn = turn)

                        val response = updateGameStatus(board, turn.next())
                        val opponentResponse = updateGameStatus(board, turn)

                        player.session.send(response.toJson())
                        opponent.session.send(opponentResponse.toJson())
                    }
                    is SocketMessage.StartTurn -> Unit
                }
            }
            else -> Unit
        }
    }
}

private fun updateGameStatus(board: Board, turn: Turn): SocketMessage {
    val width = board.columns.size
    val height = board.columns.first().slots.size

    // Horizontally score 4
    for (columnIndex in 0 until width - 3) {
        for (rowIndex in 0 until height) {
            if (
                board.columns[columnIndex].slots[rowIndex].availability == Availability.Player &&
                board.columns[columnIndex + 1].slots[rowIndex].availability == Availability.Player &&
                board.columns[columnIndex + 2].slots[rowIndex].availability == Availability.Player &&
                board.columns[columnIndex + 3].slots[rowIndex].availability == Availability.Player
            ) {
                return SocketMessage.GameOver(board, GameOverStatus.Won, turn)
            } else if (
                board.columns[columnIndex].slots[rowIndex].availability == Availability.Opponent &&
                board.columns[columnIndex + 1].slots[rowIndex].availability == Availability.Opponent &&
                board.columns[columnIndex + 2].slots[rowIndex].availability == Availability.Opponent &&
                board.columns[columnIndex + 3].slots[rowIndex].availability == Availability.Opponent
            ) {
                return SocketMessage.GameOver(board, GameOverStatus.Won, turn)
            }
        }
    }

    // Vertically score 4
    for (rowIndex in 0 until height - 3) {
        for (columnIndex in 0 until width) {
            if (
                board.columns[columnIndex].slots[rowIndex].availability == Availability.Opponent &&
                board.columns[columnIndex].slots[rowIndex + 1].availability == Availability.Opponent &&
                board.columns[columnIndex].slots[rowIndex + 2].availability == Availability.Opponent &&
                board.columns[columnIndex].slots[rowIndex + 3].availability == Availability.Opponent
            ) {
                return SocketMessage.GameOver(board, GameOverStatus.Won, turn)
            } else if (
                board.columns[columnIndex].slots[rowIndex].availability == Availability.Player &&
                board.columns[columnIndex].slots[rowIndex + 1].availability == Availability.Player &&
                board.columns[columnIndex].slots[rowIndex + 2].availability == Availability.Player &&
                board.columns[columnIndex].slots[rowIndex + 3].availability == Availability.Player
            ) {
                return SocketMessage.GameOver(board, GameOverStatus.Won, turn)
            }
        }
    }

    // Ascending diagonally score 4
    for (columnIndex in 3 until width) {
        for (rowIndex in 0 until height - 3) {
            if (
                board.columns[columnIndex].slots[rowIndex].availability == Availability.Player &&
                board.columns[columnIndex - 1].slots[rowIndex + 1].availability == Availability.Player &&
                board.columns[columnIndex - 2].slots[rowIndex + 2].availability == Availability.Player &&
                board.columns[columnIndex - 3].slots[rowIndex + 3].availability == Availability.Player
            ) {
                return SocketMessage.GameOver(board, GameOverStatus.Won, turn)
            } else if (
                board.columns[columnIndex].slots[rowIndex].availability == Availability.Opponent &&
                board.columns[columnIndex - 1].slots[rowIndex + 1].availability == Availability.Opponent &&
                board.columns[columnIndex - 2].slots[rowIndex + 2].availability == Availability.Opponent &&
                board.columns[columnIndex - 3].slots[rowIndex + 3].availability == Availability.Opponent
            ) {
                return SocketMessage.GameOver(board, GameOverStatus.Won, turn)
            }
        }
    }

    // Descending diagonally score 4
    for (columnIndex in 3 until width) {
        for (rowIndex in 3 until height) {
            if (
                board.columns[columnIndex].slots[rowIndex].availability == Availability.Player &&
                board.columns[columnIndex - 1].slots[rowIndex - 1].availability == Availability.Player &&
                board.columns[columnIndex - 2].slots[rowIndex - 2].availability == Availability.Player &&
                board.columns[columnIndex - 3].slots[rowIndex - 3].availability == Availability.Player
            ) {
                return SocketMessage.GameOver(board, GameOverStatus.Won, turn)
            } else if (
                board.columns[columnIndex].slots[rowIndex].availability == Availability.Opponent &&
                board.columns[columnIndex - 1].slots[rowIndex - 1].availability == Availability.Opponent &&
                board.columns[columnIndex - 2].slots[rowIndex - 2].availability == Availability.Opponent &&
                board.columns[columnIndex - 3].slots[rowIndex - 3].availability == Availability.Opponent
            ) {
                return SocketMessage.GameOver(board, GameOverStatus.Won, turn)
            }
        }
    }

    // Draw
    if (
        board.columns.all { column ->
            column.slots.all { slot ->
                slot.availability != Availability.Available
            }
        }
    ) {
        return SocketMessage.GameOver(board, GameOverStatus.Draw, turn)
    }

    return SocketMessage.PlayerTurn(board, turn)
}

fun SocketMessage.toJson() = Json.encodeToString(SocketMessage.serializer(), this)