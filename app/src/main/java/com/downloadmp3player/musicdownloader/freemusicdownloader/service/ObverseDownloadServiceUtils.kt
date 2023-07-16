package com.downloadmp3player.musicdownloader.freemusicdownloader.service

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.DownloadProgress
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline

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