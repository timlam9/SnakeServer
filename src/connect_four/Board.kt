package com.beatsnake.connect_four

import kotlinx.serialization.Serializable

@Serializable
data class Board(val columns: List<Column>) {

    companion object {

        fun generateEmptyBoard(): Board {
            val columns = mutableListOf<Column>()
            repeat(7) {
                val slots = mutableListOf<Slot>()
                repeat(6) { slot ->
                    slots.add(Slot(index = slot))
                }
                columns.add(Column(slots = slots))
            }
            return Board(columns = columns)
        }

    }

    fun update(updatedSlot: Int, turn: Turn): Board = Board(
        columns = columns.mapIndexed { index, column ->
            if (index == updatedSlot) {
                val emptySlot = column.slots.lastOrNull { it.availability == Availability.Available }
                if (emptySlot == null) {
                    throw ColumnAlreadyFilledException
                } else {
                    column.copy(slots = column.slots.map { if (it == emptySlot) it.claim(turn) else it })
                }
            } else
                column
        }
    )

}

object ColumnAlreadyFilledException : Exception() {

    override val message: String
        get() = "Cannot select an already filled board"

}

@Serializable
data class Column(val slots: List<Slot>)

@Serializable
data class Slot(
    val index: Int,
    val availability: Availability = Availability.Available
) {

    fun claim(turn: Turn): Slot = when (turn) {
        Turn.Player -> Slot(index = index, availability = Availability.Player)
        Turn.Opponent -> Slot(index = index, availability = Availability.Opponent)
    }

}
enum class Availability {
    Available,
    Player,
    Opponent
}

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

