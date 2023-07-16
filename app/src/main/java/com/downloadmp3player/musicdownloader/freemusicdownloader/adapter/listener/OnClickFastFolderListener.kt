package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener

import com.downloadmp3player.musicdownloader.freemusicdownloader.model.FolderItem

interface OnClickFastFolderListener {
    fun onFastFolderClick(folder: FolderItem, pos: Int)
}