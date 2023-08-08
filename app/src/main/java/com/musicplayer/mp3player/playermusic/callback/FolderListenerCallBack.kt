package com.musicplayer.mp3player.playermusic.callback

import com.musicplayer.mp3player.playermusic.model.FolderItem
import com.musicplayer.mp3player.playermusic.model.MusicItem

interface FolderListenerCallBack {
    fun onFolderClick(folder: FolderItem, i: Int)

    fun onSongClick(song: MusicItem, pos: Int)

    fun onFolderMoreClick(folder: FolderItem, i: Int)
}