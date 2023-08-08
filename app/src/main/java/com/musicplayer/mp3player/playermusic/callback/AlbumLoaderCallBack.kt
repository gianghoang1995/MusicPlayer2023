package com.musicplayer.mp3player.playermusic.callback

import com.musicplayer.mp3player.playermusic.model.AlbumItem

interface AlbumLoaderCallBack {
    fun onLoadAlbumSuccessful(listAlbum: ArrayList<AlbumItem>)
}