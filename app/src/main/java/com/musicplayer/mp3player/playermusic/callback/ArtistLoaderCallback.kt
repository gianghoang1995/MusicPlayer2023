package com.musicplayer.mp3player.playermusic.callback

import com.musicplayer.mp3player.playermusic.model.ArtistItem

interface ArtistLoaderCallback {
    fun onLoadArtistSuccessful(listArtist: ArrayList<ArtistItem>)
}