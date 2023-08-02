package com.musicplayer.mp3player.defaultmusicplayer.adapter.listener

import android.view.View
import com.musicplayer.mp3player.defaultmusicplayer.model.PlaylistITem

interface OnClickPlaylistListener {
    fun onPlaylistClick(favorite: PlaylistITem, i: Int)

    fun onPlaylistMoreClick(favorite: PlaylistITem, view:View)
}