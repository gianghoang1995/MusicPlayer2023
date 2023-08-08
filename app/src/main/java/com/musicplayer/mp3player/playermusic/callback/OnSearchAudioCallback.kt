package com.musicplayer.mp3player.playermusic.callback

import com.musicplayer.mp3player.playermusic.model.MusicItem

interface OnSearchAudioCallback {
    fun onSearchAudioSuccessful(songList: ArrayList<MusicItem>)
}