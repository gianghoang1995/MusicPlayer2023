package com.musicplayer.mp3player.defaultmusicplayer.adapter.listener

import android.view.View
import com.google.android.exoplayer2.MediaMetadata

interface OnClickNowPlayingListener {
    fun onClickItemNowPlaying(song: MediaMetadata, i: Int)

    fun onClickMoreItemNowPlaying(song: MediaMetadata, i: Int, view: View)

    fun onSongOnLongClick(isSelect: Boolean)

    fun onSizeSelectChange(size: Int)
}