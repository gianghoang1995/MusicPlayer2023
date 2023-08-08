package com.musicplayer.mp3player.playermusic.ui.fragment.main.folder.loader

import com.musicplayer.mp3player.playermusic.model.FolderItem

interface ScaningFolderListener {
    fun onScanningMusicFolderSuccess(result:ArrayList<FolderItem>)
}