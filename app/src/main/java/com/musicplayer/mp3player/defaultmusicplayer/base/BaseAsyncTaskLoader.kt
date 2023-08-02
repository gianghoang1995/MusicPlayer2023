package com.musicplayer.mp3player.defaultmusicplayer.base

import android.content.Context
import androidx.loader.content.AsyncTaskLoader

abstract class BaseAsyncTaskLoader<T>(context: Context?) :
    AsyncTaskLoader<T>(context!!) {
    protected var mData: T? = null
    private var filter: String? = null

    override fun deliverResult(data: T?) {
        if (isReset) {
            data?.let { onReleaseResources(it) }
        }
        val oldData = mData
        mData = data
        if (isStarted) {
            super.deliverResult(data)
        }
        oldData?.let { onReleaseResources(it) }
    }

    override fun onStartLoading() {
        if (mData != null) {
            deliverResult(mData)
        }
        if (takeContentChanged() || mData == null) {
            forceLoad()
        }
    }

    override fun onStopLoading() {
        cancelLoad()
    }

    override fun onCanceled(data: T?) {
        super.onCanceled(data)
        data?.let { onReleaseResources(it) }
    }

    override fun onReset() {
        super.onReset()
        onStopLoading()
        if (mData != null) {
            onReleaseResources(mData!!)
            mData = null
        }
    }

    protected fun onReleaseResources(apps: T) {
    }
}