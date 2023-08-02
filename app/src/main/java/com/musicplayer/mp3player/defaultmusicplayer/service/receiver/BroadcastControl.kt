package com.musicplayer.mp3player.defaultmusicplayer.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.musicplayer.mp3player.defaultmusicplayer.eventbus.EventControlClicker
import com.musicplayer.mp3player.defaultmusicplayer.utils.AppConstants
import org.greenrobot.eventbus.EventBus

class BroadcastControl : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action == AppConstants.ACTION_NEXT) {
            EventBus.getDefault().postSticky(EventControlClicker(AppConstants.ACTION_NEXT))
        } else if (intent.action == AppConstants.ACTION_TOGGLE_PLAY) {
            EventBus.getDefault().postSticky(EventControlClicker(AppConstants.ACTION_TOGGLE_PLAY))
        } else if (intent.action == AppConstants.ACTION_PRIVE) {
            EventBus.getDefault().postSticky(EventControlClicker(AppConstants.ACTION_PRIVE))
        } else if (intent.action == AppConstants.ACTION_STOP) {
            EventBus.getDefault().postSticky(EventControlClicker(AppConstants.ACTION_STOP))
        }
    }
}