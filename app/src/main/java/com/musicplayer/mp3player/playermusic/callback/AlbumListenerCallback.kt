package com.musicplayer.mp3player.playermusic.callback

import android.view.View
import com.musicplayer.mp3player.playermusic.model.AlbumItem

interface AlbumListenerCallback {
    fun onAlbumMoreClick(album: AlbumItem, i: Int, view: View)
    fun onAlbumClick(album: AlbumItem, i: Int)
}