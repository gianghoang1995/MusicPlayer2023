package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener

import android.view.View
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline

interface OnClickSongListener {
    fun onSongClick(song: MusicItem, i: Int)

    fun onSongMoreClick(item: Any, i: Int, view: View)

    fun onSongOnLongClick()

    fun onSizeSelectChange(size: Int)

    fun onClickItemOnline(itemOnline: ItemMusicOnline)
}