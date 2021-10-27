package com.beatsnake.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val name: String,
    val email: String,
    val highscore: Int = 0
)