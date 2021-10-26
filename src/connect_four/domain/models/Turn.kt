package com.beatsnake.connect_four.domain.models

enum class Turn {
    Player,
    Opponent;

    override fun toString(): String {
        return "$name's turn!"
    }

    fun next(): Turn = when (this) {
        Player -> Opponent
        Opponent -> Player
    }

}