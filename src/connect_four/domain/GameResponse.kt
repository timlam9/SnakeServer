package com.beatsnake.connect_four.domain

import com.beatsnake.connect_four.data.SocketMessage
import com.beatsnake.connect_four.domain.models.Game

sealed class GameResponse {

    data class UpdatedGame(
        val game: Game,
        val playerResponse: SocketMessage.OutBound,
        val opponentResponse: SocketMessage.OutBound,
    ) : GameResponse()

    data class Error(val value: SocketMessage.OutBound.SocketError) : GameResponse()

}