package com.musicplayer.mp3player.defaultmusicplayer.adapter.listener

import android.view.View
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem
import com.musicplayer.mp3player.defaultmusicplayer.model.ItemMusicOnline

interface OnClickSongListener {
    fun onSongClick(song: MusicItem, i: Int)

    fun onSongMoreClick(item: Any, i: Int, view: View)

    fun onSongOnLongClick()

    fun onSizeSelectChange(size: Int)

    fun onClickItemOnline(itemOnline: ItemMusicOnline)
}