package com.beatsnake.connect_four.domain

import com.beatsnake.connect_four.data.SocketMessage.OutBound.SocketError
import com.beatsnake.connect_four.domain.models.Game
import com.beatsnake.connect_four.domain.models.Turn

class ScoreFourEngine {

    fun handleMove(preMoveGame: Game, email: String, move: Int): GameResponse {
        var game = preMoveGame

        val turn = when (email) {
            game.player.email -> Turn.Player
            game.opponent.email -> Turn.Opponent
            else -> Turn.Opponent
        }

        return try {
            game = game.copy(board = game.board.update(move, turn))

            val playerResponse = game.board.updateGameStatus(turn.next())
            val opponentResponse = game.board.updateGameStatus(turn)

            GameResponse.UpdatedGame(
                game = game,
                playerResponse = playerResponse,
                opponentResponse = opponentResponse
            )
        } catch (e: ColumnAlreadyFilledException) {
            GameResponse.Error(SocketError("ColumnAlreadyFilled"))
        }
    }

}

