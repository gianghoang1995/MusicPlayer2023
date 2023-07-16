package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener

import android.view.View
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.AlbumItem

interface OnClickAlbumListener {
    fun onAlbumClick(album: AlbumItem, i: Int)

    fun onAlbumMoreClick(album: AlbumItem, i: Int, view: View)
}