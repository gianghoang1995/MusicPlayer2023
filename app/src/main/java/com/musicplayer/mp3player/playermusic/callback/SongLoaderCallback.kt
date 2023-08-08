package com.musicplayer.mp3player.playermusic.callback

import com.musicplayer.mp3player.playermusic.model.MusicItem
import java.util.*

interface SongLoaderCallback {
    fun onAudioLoadedSuccessful(songList: ArrayList<MusicItem>)
}