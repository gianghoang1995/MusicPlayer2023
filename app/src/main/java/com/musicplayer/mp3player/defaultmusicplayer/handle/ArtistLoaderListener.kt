package com.musicplayer.mp3player.defaultmusicplayer.handle

import com.musicplayer.mp3player.defaultmusicplayer.model.ArtistItem

interface ArtistLoaderListener {
    fun onLoadArtistSuccessful(listArtist: ArrayList<ArtistItem>)
}