package com.beatsnake.connect_four.domain

import com.beatsnake.connect_four.data.Connection
import com.beatsnake.connect_four.data.SocketMessage.InBound.Disconnect
import com.beatsnake.connect_four.data.SocketMessage.OutBound
import com.beatsnake.connect_four.data.SocketMessage.OutBound.*
import com.beatsnake.connect_four.domain.models.ColumnAlreadyFilledException
import com.beatsnake.connect_four.domain.models.Game
import com.beatsnake.connect_four.domain.models.Turn
import connect_four.domain.models.Board
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class ScoreFourEngine {

    private val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
    private var games: MutableList<Game> = mutableListOf()
    private var creatingGame = MutableStateFlow(false)

    fun createConnection(serverSession: DefaultWebSocketServerSession, email: String) {
        val thisConnection = Connection(serverSession, email)
        connections += thisConnection
    }

    suspend fun createNewGameIfPossible() {
        if (connections.size >= 2 && !creatingGame.value) createNewGame()
    }

    suspend fun sendDisconnectionMessageAndDestroyGame(message: Disconnect) {
        val game = findGameByPlayerEmail(message.email) ?: return
        connections.removeIf { it.email == message.email }
        games.remove(game)

        val error = SocketError("ConnectionLost").toJson()
        when (game.player.email) {
            message.email -> game.opponent.session.send(error)
            else -> game.player.session.send(error)
        }
    }

    suspend fun handleMove(email: String, move: Int) {
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
                game.opponent.email -> game.opponent.session.send(
                    SocketError("ColumnAlreadyFilled").toJson()
                )
            }
        }
    }

    private suspend fun createNewGame() {
        creatingGame.value = true

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

        creatingGame.value = false
    }

    private fun findGameByGameID(gameID: String): Game? = games.find { it.gameID == gameID }

    private fun findGameByPlayerEmail(email: String) = games.find {
        it.player.email == email || it.opponent.email == email
    }

    private fun removeGameWhenGameIsOver(
        playerResponse: OutBound,
        opponentResponse: OutBound,
        game: Game
    ) {
        if (
            playerResponse is GameOver ||
            opponentResponse is GameOver
        ) {
            games.remove(findGameByGameID(game.gameID))
        }
    }

    private fun MutableSet<Connection>.getAndRemoveFirst(): Connection = first().apply { remove(first()) }

    private fun Game.encodeInitialResponse(turn: Turn) = StartTurn(
        board = board,
        turn = turn
    ).toJson()

    private fun MutableList<Game>.updateGame(game: Game) {
        remove(findGameByGameID(game.gameID))
        add(game)
    }

}
