package com.musicplayer.mp3player.defaultmusicplayer.adapter.listener

import com.musicplayer.mp3player.defaultmusicplayer.model.FolderItem
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem

interface OnClickFolderListener {
    fun onFolderClick(folder: FolderItem, i: Int)

    fun onSongClick(song: MusicItem, pos: Int)

    fun onFolderMoreClick(folder: FolderItem, i: Int)
}