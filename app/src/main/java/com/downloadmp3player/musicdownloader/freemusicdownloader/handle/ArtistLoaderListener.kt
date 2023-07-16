package com.downloadmp3player.musicdownloader.freemusicdownloader.handle

import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ArtistItem

interface ArtistLoaderListener {
    fun onLoadArtistSuccessful(listArtist: ArrayList<ArtistItem>)
}