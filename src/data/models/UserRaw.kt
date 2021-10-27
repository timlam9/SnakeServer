package com.beatsnake.data.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class UserRaw(
    @BsonId
    val id: String = ObjectId().toString(),
    val email: String = "",
    val name: String = "",
    val password: String = "",
    val highscore: Int = 0
) {

    fun toUser() = User(
        id = id,
        name = name,
        email = email,
        highscore = highscore
    )

}

