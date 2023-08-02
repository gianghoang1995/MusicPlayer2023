package com.musicplayer.mp3player.defaultmusicplayer.widget

import android.content.Context
import android.content.res.Resources
import android.database.ContentObserver
import android.os.Handler
import android.os.SystemClock
import android.provider.Settings
import android.text.format.DateFormat
import android.util.AttributeSet
import java.util.*

class CustomDigitalClock : androidx.appcompat.widget.AppCompatTextView {
    var mCalendar: Calendar? = null
    private lateinit var mFormatChangeObserver: FormatChangeObserver
    lateinit var mTicker: Runnable
    lateinit var mHandler: Handler
    private var mTickerStopped = false
    var mFormat: String? = null

    constructor(context: Context) : super(context) {
        initClock(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initClock(context)
    }

    private fun initClock(context: Context) {
        val r: Resources = context.resources
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance()
        }
        mFormatChangeObserver = FormatChangeObserver()
        getContext().contentResolver.registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver
        )
    }

    override fun onAttachedToWindow() {
        mTickerStopped = false
        super.onAttachedToWindow()
        mHandler = Handler()
        /**
         * requests a tick on the next hard-second boundary
         */
        mTicker = Runnable {
            if (mTickerStopped) return@Runnable
            mCalendar?.timeInMillis = System.currentTimeMillis()
            text = DateFormat.format(mFormat, mCalendar)
            invalidate()
            val now: Long = SystemClock.uptimeMillis()
            val next = now + (1000 - now % 1000)
            mHandler.postAtTime(mTicker, next)
        }
        mTicker.run()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mTickerStopped = true
    }

    /**
     * Pulls 12/24 mode from system settings
     */
    private fun get24HourMode(): Boolean {
        return DateFormat.is24HourFormat(context)
    }

    fun setTimeFormat(format: String) {
        mFormat = format
        /* mFormat = if (get24HourMode()) {
             m24
         } else {
             m12
         }*/
    }

    inner class FormatChangeObserver : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
//            setFormat()
        }
    }

    companion object {
        const val m12 = "h:mm aa"
        const val m24 = "k:mm"
    }
}