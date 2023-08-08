package com.musicplayer.mp3player.playermusic.callback

import com.musicplayer.mp3player.playermusic.model.FolderItem

interface FastFolderListenerCallBack {
    fun onFastFolderClick(folder: FolderItem, pos: Int)
}