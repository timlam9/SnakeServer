package com.beatsnake.connect_four.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Column(val slots: List<Slot>)