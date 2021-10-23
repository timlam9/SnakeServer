package com.beatsnake.connect_four

data class Game(
    val gameID: String,
    val board: Board,
    val player: Connection,
    val opponent: Connection
)