package com.downloadmp3player.musicdownloader.freemusicdownloader.handle

import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
import java.util.*

interface SongLoaderListener {
    fun onAudioLoadedSuccessful(songList: ArrayList<MusicItem>)
}