package com.downloadmp3player.musicdownloader.freemusicdownloader.model

data class LanguageModel(
    val image: Int,
    val language: String,
    var code: String,
    var isSelected: Boolean = false
) {
}