package com.downloadmp3player.musicdownloader.freemusicdownloader.handle

import com.downloadmp3player.musicdownloader.freemusicdownloader.model.AlbumItem

interface AlbumLoaderListener {
    fun onLoadAlbumSuccessful(listAlbum: ArrayList<AlbumItem>)
}