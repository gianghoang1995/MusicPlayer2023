package com.musicplayer.mp3player.defaultmusicplayer.handle

import com.musicplayer.mp3player.defaultmusicplayer.model.AlbumItem

interface AlbumLoaderListener {
    fun onLoadAlbumSuccessful(listAlbum: ArrayList<AlbumItem>)
}