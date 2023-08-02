package com.musicplayer.mp3player.defaultmusicplayer.adapter.listener

import android.view.View
import com.musicplayer.mp3player.defaultmusicplayer.model.AlbumItem

interface OnClickAlbumListener {
    fun onAlbumClick(album: AlbumItem, i: Int)

    fun onAlbumMoreClick(album: AlbumItem, i: Int, view: View)
}