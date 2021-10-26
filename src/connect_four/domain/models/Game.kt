package com.beatsnake.connect_four.domain.models

import com.beatsnake.connect_four.data.Connection
import connect_four.domain.models.Board

data class Game(
    val gameID: String,
    val board: Board,
    val player: Connection,
    val opponent: Connection
)