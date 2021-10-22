package com.beatsnake.connect_four

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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

    fun toJson() = Json.encodeToString(serializer(), this)

}
