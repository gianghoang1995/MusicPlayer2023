package com.musicplayer.mp3player.defaultmusicplayer.service

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.musicplayer.mp3player.defaultmusicplayer.model.DownloadProgress
import com.musicplayer.mp3player.defaultmusicplayer.model.ItemMusicOnline

class ObverseDownloadServiceUtils(var context: Context) {
    var downloadingState = MutableLiveData(0)
    val _downloadingStatus: LiveData<Int> get() = downloadingState

    var progressDownloadUpdate = MutableLiveData<DownloadProgress?>(null)
    val getProgressDownload: LiveData<DownloadProgress?> get() = progressDownloadUpdate

    var currentItemAudio = MutableLiveData<ItemMusicOnline?>(null)
    val getCurrentItemAudio: LiveData<ItemMusicOnline?> get() = currentItemAudio

    fun resetObverse() {
        downloadingState.postValue(0)
        currentItemAudio.postValue(null)
        progressDownloadUpdate.postValue(null)
    }
}