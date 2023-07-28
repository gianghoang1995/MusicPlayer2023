package com.downloadmp3player.musicdownloader.freemusicdownloader.service

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ExoPlayer
import kotlin.collections.ArrayList

class ObverseMusicUtils(var context: Context) {
    var _exoPlayer = MutableLiveData<ExoPlayer?>(null)
    val getPlayer: LiveData<ExoPlayer?> get() = _exoPlayer

    var currentItemAudio = MutableLiveData<Any?>(null)
    val getCurrentItemAudio: LiveData<Any?> get() = currentItemAudio

    var playbackState = MutableLiveData<Int>()
    val getPlaybackState: LiveData<Int> get() = playbackState

    var listData = MutableLiveData<ArrayList<Any?>>()
    val getListData: LiveData<ArrayList<Any?>> get() = listData

    var isRunningTimer = MutableLiveData(false)
    val _isRunningTimer: LiveData<Boolean> get() = isRunningTimer

    var isEndSong = MutableLiveData(false)
    val _isEndSong: LiveData<Boolean> get() = isEndSong

    var isServiceRunning = MutableLiveData(false)
    val _isServiceRunning: LiveData<Boolean> get() = isServiceRunning

    var insertRecentlyPlayed = MutableLiveData(false)
    val _insertRecentlyPlayed: LiveData<Boolean> get() = insertRecentlyPlayed

    var insertFavoriteStream = MutableLiveData(false)
    val getInsertFavoriteStream: LiveData<Boolean> get() = insertFavoriteStream

    fun resetObverse(){
        _exoPlayer.postValue(null)
        currentItemAudio.postValue(null)
        playbackState.postValue(-1)
        listData.postValue(null)
        isRunningTimer.postValue(false)
        isServiceRunning.postValue(false)
    }
}