package com.beatsnake.connect_four

object ColumnAlreadyFilledException : Exception() {

    override val message: String
        get() = "Cannot select an already filled board"

}