package com.beatsnake.domain

// JWT
const val JWT_AUTH = "auth-jwt"
const val JWT_REALM = "jwt.realm"
val secret: String = System.getenv("SECRET")
val audience: String = System.getenv("AUDIENCE")
val issuer: String = System.getenv("ISSUER")

// Constants
const val EMPTY = ""
const val USERNAME = "username"
const val EMAIL = "email"
const val ID = "id"
const val NO_USER_FOUND = "No user found"
const val FIFTEEN_SECONDS: Long = 15

// Routes
const val USER = "/user"
const val USERS = "/users"
const val POST_USER = "/postUser"
const val REFRESH_TOKEN = "/refreshToken"