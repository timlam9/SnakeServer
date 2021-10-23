package com.beatsnake.connect_four

import io.ktor.http.cio.websocket.*

data class Connection(
    val session: DefaultWebSocketSession,
    val id: String = ""
)