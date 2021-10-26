package com.beatsnake.domain

import kotlinx.serialization.Serializable

@Serializable
data class RegisteredUser(
    val name: String,
    val email: String,
    val token: String
)