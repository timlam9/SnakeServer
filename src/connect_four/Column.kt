package com.beatsnake.connect_four

import kotlinx.serialization.Serializable

@Serializable
data class Column(val slots: List<Slot>)