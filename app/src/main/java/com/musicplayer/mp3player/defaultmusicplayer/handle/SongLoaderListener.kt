package com.musicplayer.mp3player.defaultmusicplayer.handle

import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem
import java.util.*

interface SongLoaderListener {
    fun onAudioLoadedSuccessful(songList: ArrayList<MusicItem>)
}