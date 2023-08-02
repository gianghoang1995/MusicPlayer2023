package com.musicplayer.mp3player.defaultmusicplayer.handle

import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem

interface OnSearchAudioListener {
    fun onSearchAudioSuccessful(songList: ArrayList<MusicItem>)
}