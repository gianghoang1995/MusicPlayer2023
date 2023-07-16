package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener

import com.downloadmp3player.musicdownloader.freemusicdownloader.model.FolderItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem

interface OnClickFolderListener {
    fun onFolderClick(folder: FolderItem, i: Int)

    fun onSongClick(song: MusicItem, pos: Int)

    fun onFolderMoreClick(folder: FolderItem, i: Int)
}