package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.folder.loader

import com.downloadmp3player.musicdownloader.freemusicdownloader.model.FolderItem

interface ScaningFolderListener {
    fun onScanningMusicFolderSuccess(result:ArrayList<FolderItem>)
}