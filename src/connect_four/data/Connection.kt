package com.beatsnake.connect_four.data

import io.ktor.http.cio.websocket.*

data class Connection(
    val session: DefaultWebSocketSession,
    val id: String = ""
)