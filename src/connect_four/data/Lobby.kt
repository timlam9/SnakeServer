package com.beatsnake.connect_four.data

import com.beatsnake.connect_four.domain.models.Game
import com.beatsnake.connect_four.domain.models.Turn
import connect_four.domain.models.Board
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

class Lobby(private val application: Application) {

    private val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
    private var games: MutableList<Game> = mutableListOf()
    private var creatingGame = MutableStateFlow(false)

    suspend fun createConnection(serverSession: DefaultWebSocketServerSession, email: String) {
        val thisConnection = Connection(serverSession, email)
        connections += thisConnection

        application.log.info(" ")
        application.log.info("------ ------ ------ ------ ------ ------ Connections: ${connections.size}")
        application.log.info(" ")

        if (canCreateNewGame()) createNewGame()
    }

    suspend fun sendDisconnectionMessageAndDestroyGame(message: SocketMessage.InBound.Disconnect) {
        val game = findGameByPlayerEmail(message.email) ?: return
        connections.removeIf { it.email == message.email }
        games.remove(game)

        val error = SocketMessage.OutBound.SocketError("ConnectionLost").toJson()
        when (game.player.email) {
            message.email -> game.opponent.session.send(error)
            else -> game.player.session.send(error)
        }
    }

    fun updateGame(game: Game) {
        games.remove(findGameByGameID(game.gameID))
        games.add(game)
    }

    fun removeGameWhenGameIsOver(
        game: Game,
        playerResponse: SocketMessage.OutBound,
        opponentResponse: SocketMessage.OutBound
    ) {
        if (
            playerResponse is SocketMessage.OutBound.GameOver ||
            opponentResponse is SocketMessage.OutBound.GameOver
        ) {
            games.remove(findGameByGameID(game.gameID))
        }
    }

    fun findGameByPlayerEmail(email: String) = games.find {
        it.player.email == email || it.opponent.email == email
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

    private fun canCreateNewGame() = connections.size >= 2 && !creatingGame.value

    private fun findGameByGameID(gameID: String): Game? = games.find { it.gameID == gameID }

    private fun MutableSet<Connection>.getAndRemoveFirst(): Connection = first().apply { remove(first()) }

    private fun Game.encodeInitialResponse(turn: Turn) = SocketMessage.OutBound.StartTurn(
        board = board,
        turn = turn
    ).toJson()

}