package com.musicplayer.mp3player.defaultmusicplayer.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.musicplayer.mp3player.defaultmusicplayer.eventbus.EventShowLockScreen
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ScreenActionReceiver : BroadcastReceiver() {
    private val TAG = "ScreenActionReceiver"

    override fun onReceive(context: Context?, intent: Intent) {
        val action = intent.action
        when {
            Intent.ACTION_SCREEN_ON == action -> {
                Log.e(TAG, "screen is on...")
            }
            Intent.ACTION_SCREEN_OFF == action -> {
                Log.e(TAG, "screen is off...")
                EventBus.getDefault().postSticky(EventShowLockScreen(true))
            }
        }
    }

    val filter: IntentFilter
        get() {
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            filter.addAction(Intent.ACTION_SCREEN_ON)
            return filter
        }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun eventScreen(event: EventShowLockScreen) {

    }
}