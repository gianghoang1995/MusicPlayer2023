package com.musicplayer.mp3player.playermusic.model

data class LanguageModel(
    val image: Int,
    val language: String,
    var code: String,
    var isSelected: Boolean = false
) {
}