package com.musicplayer.mp3player.playermusic.callback

import android.view.View
import com.musicplayer.mp3player.playermusic.model.PlaylistITem

interface PlaylistCallBack {
    fun onPlaylistClick(favorite: PlaylistITem, i: Int)
    fun onPlaylistMoreClick(favorite: PlaylistITem, view:View)
}