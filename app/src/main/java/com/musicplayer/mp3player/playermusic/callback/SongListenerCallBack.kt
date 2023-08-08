package com.musicplayer.mp3player.playermusic.callback

import android.view.View
import com.musicplayer.mp3player.playermusic.model.MusicItem
import com.musicplayer.mp3player.playermusic.model.ItemMusicOnline

interface SongListenerCallBack {
    fun onSongClick(song: MusicItem, i: Int)
    fun onSongMoreClick(item: Any, i: Int, view: View)
    fun onSongOnLongClick()
    fun onSizeSelectChange(size: Int)
    fun onClickItemOnline(itemOnline: ItemMusicOnline)
}