package com.musicplayer.mp3player.defaultmusicplayer.adapter.listener

import android.view.View
import com.musicplayer.mp3player.defaultmusicplayer.model.ArtistItem

interface OnClickArtistListener {
    fun onArtistClick(artist: ArtistItem, i: Int)

    fun onArtistMoreClick(artist: ArtistItem, i: Int, view: View)
}