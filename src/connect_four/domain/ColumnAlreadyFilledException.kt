package com.beatsnake.connect_four.domain

object ColumnAlreadyFilledException : Exception() {

    override val message: String
        get() = "Cannot select an already filled board"

}