package com.beatsnake.data.models

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationCredentials(val name: String, val email: String, val password: String)

@Serializable
data class RefreshTokenCredentials(val name: String, val email: String)