package com.musicplayer.mp3player.defaultmusicplayer.adapter.listener

import com.musicplayer.mp3player.defaultmusicplayer.model.FolderItem

interface OnClickFastFolderListener {
    fun onFastFolderClick(folder: FolderItem, pos: Int)
}