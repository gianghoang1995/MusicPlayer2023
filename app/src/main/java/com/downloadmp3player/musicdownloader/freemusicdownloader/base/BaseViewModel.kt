package com.downloadmp3player.musicdownloader.freemusicdownloader.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job

abstract class BaseViewModel : ViewModel() {
    val isLoading = MutableLiveData<Boolean>()
    val loadFileException = MutableLiveData<String>()
    protected var parentJob: Job? = null

    protected fun registerEventParentJobFinish() {
        parentJob?.invokeOnCompletion {
            isLoading.postValue(false)
        }
    }

    protected val loadFileExceptionHandle = CoroutineExceptionHandler { coroutineContext, throwable ->

    }
}