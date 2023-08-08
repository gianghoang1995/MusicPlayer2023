package com.musicplayer.mp3player.playermusic.callback

import android.view.View
import com.musicplayer.mp3player.playermusic.model.ArtistItem

interface ArtistListenerCallBack {
    fun onArtistMoreClick(artist: ArtistItem, i: Int, view: View)
    fun onArtistClick(artist: ArtistItem, i: Int)
}