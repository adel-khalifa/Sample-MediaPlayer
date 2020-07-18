package com.greycom.samplemediaplayer

import java.io.Serializable

data class Song(
    val title: String,
    val artist: String,
    val path: String,
    val photo: String?

) : Serializable