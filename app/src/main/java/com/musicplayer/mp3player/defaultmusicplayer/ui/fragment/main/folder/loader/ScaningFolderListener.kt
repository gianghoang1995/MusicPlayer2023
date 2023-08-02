package com.musicplayer.mp3player.defaultmusicplayer.ui.fragment.main.folder.loader

import com.musicplayer.mp3player.defaultmusicplayer.model.FolderItem

interface ScaningFolderListener {
    fun onScanningMusicFolderSuccess(result:ArrayList<FolderItem>)
}