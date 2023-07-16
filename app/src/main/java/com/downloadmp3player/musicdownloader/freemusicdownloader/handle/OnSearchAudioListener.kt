package com.downloadmp3player.musicdownloader.freemusicdownloader.handle

import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem

interface OnSearchAudioListener {
    fun onSearchAudioSuccessful(songList: ArrayList<MusicItem>)
}