package com.downloadmp3player.musicdownloader.freemusicdownloader.utils

import android.content.ContentUris
import android.net.Uri

object ArtworkUtils {

    fun getAlbumArtUri(album_id: Int): Uri {
        val songCover = Uri.parse("content://media/external/audio/albumart")
        return ContentUris.withAppendedId(songCover, album_id.toLong())

    }

    fun uri(key: Long): Uri {
        val albumCover = Uri.parse("content://media/external/audio/albumart")
        return ContentUris.withAppendedId(albumCover, key)
    }

    fun getArtworkFromSongID(songID: Long): Uri? {
        val uri = Uri.parse("content://media/external/audio/media/$songID/albumart")
        return uri
    }
}