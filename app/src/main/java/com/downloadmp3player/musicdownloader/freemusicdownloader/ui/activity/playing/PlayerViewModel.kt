package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.playing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayerViewModel : ViewModel() {
    var isNewThumb = MutableLiveData(false)
    val _isNewThumb: LiveData<Boolean> get() = isNewThumb
}