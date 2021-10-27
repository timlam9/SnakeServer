package com.beatsnake.data.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthToken(val value: String)