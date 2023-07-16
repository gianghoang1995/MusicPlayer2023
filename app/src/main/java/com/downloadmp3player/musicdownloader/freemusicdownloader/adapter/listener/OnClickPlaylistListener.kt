package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener

import android.view.View
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.PlaylistITem

interface OnClickPlaylistListener {
    fun onPlaylistClick(favorite: PlaylistITem, i: Int)

    fun onPlaylistMoreClick(favorite: PlaylistITem, view:View)
}