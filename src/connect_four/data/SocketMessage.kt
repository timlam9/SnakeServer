package com.beatsnake.connect_four.data

import com.beatsnake.connect_four.domain.models.GameOverStatus
import com.beatsnake.connect_four.domain.models.Turn
import connect_four.domain.models.Board
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed class SocketMessage {

    @Serializable
    @SerialName("connect")
    data class Connect(val email: String) : SocketMessage()

    @Serializable
    @SerialName("disconnect")
    data class Disconnect(val email: String) : SocketMessage()

    @Serializable
    @SerialName("move")
    data class Move(val email: String, val move: Int) : SocketMessage()

    @Serializable
    @SerialName("start_turn")
    data class StartTurn(val board: Board, val turn: Turn) : SocketMessage()

    @Serializable
    @SerialName("player_turn")
    data class PlayerTurn(val board: Board, val turn: Turn) : SocketMessage()

    @Serializable
    @SerialName("game_over")
    data class GameOver(val board: Board, val winner: GameOverStatus, val turn: Turn) : SocketMessage()

    @Serializable
    @SerialName("socket_error")
    data class SocketError(val errorType: String) : SocketMessage()

    fun toJson() = Json.encodeToString(serializer(), this)

}
