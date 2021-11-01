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
    sealed class InBound: SocketMessage() {

        @Serializable
        @SerialName("connect")
        data class Connect(val email: String) : InBound()

        @Serializable
        @SerialName("disconnect")
        data class Disconnect(val email: String) : InBound()

        @Serializable
        @SerialName("move")
        data class Move(val email: String, val move: Int) : InBound()

    }

    @Serializable
    sealed class OutBound: SocketMessage() {

        @Serializable
        @SerialName("start_turn")
        data class StartTurn(val board: Board, val turn: Turn) : OutBound()

        @Serializable
        @SerialName("player_turn")
        data class PlayerTurn(val board: Board, val turn: Turn) : OutBound()

        @Serializable
        @SerialName("game_over")
        data class GameOver(val board: Board, val winner: GameOverStatus, val turn: Turn) : OutBound()

        @Serializable
        @SerialName("socket_error")
        data class SocketError(val errorType: String) : OutBound()

    }

    fun toJson() = Json.encodeToString(serializer(), this)

}
