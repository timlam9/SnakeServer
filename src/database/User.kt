package com.beatsnake.database

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    @BsonId
    val id: String = ObjectId().toString(),
    val name: String = "",
    val highscore: Int = 0
)