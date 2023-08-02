package com.musicplayer.mp3player.defaultmusicplayer.ui.activity.playing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayerViewModel : ViewModel() {
    var isNewThumb = MutableLiveData(false)
    val _isNewThumb: LiveData<Boolean> get() = isNewThumb
}