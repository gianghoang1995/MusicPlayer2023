package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener

import android.view.View
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ArtistItem

interface OnClickArtistListener {
    fun onArtistClick(artist: ArtistItem, i: Int)

    fun onArtistMoreClick(artist: ArtistItem, i: Int, view: View)
}