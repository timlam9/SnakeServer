package com.beatsnake.connect_four

import kotlinx.serialization.Serializable

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