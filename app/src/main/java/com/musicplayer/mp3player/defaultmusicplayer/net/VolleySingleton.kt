package com.musicplayer.mp3player.defaultmusicplayer.net

import android.annotation.SuppressLint
import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleySingleton private constructor(private var mContext: Context) {
    private var mRequestQueue: RequestQueue?

    init {
        mRequestQueue = requestQueue
    }

    private val requestQueue: RequestQueue
        get() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(mContext)
            }
            return mRequestQueue as RequestQueue
        }

    fun <T> addToRequestQueue(request: Request<T>?) {
        requestQueue.add(request)
    }


    companion object {
        @SuppressLint("StaticFieldLeak")
        var mInstance: VolleySingleton? = null
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): VolleySingleton? {
            if (mInstance == null) {
                mInstance = VolleySingleton(context)
            }
            return mInstance
        }
    }
}