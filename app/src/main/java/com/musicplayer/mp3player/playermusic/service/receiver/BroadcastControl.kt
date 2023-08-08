package com.musicplayer.mp3player.playermusic.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.musicplayer.mp3player.playermusic.eventbus.BusControlClicker
import com.musicplayer.mp3player.playermusic.utils.AppConstants
import org.greenrobot.eventbus.EventBus

class BroadcastControl : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action == AppConstants.ACTION_NEXT) {
            EventBus.getDefault().postSticky(BusControlClicker(AppConstants.ACTION_NEXT))
        } else if (intent.action == AppConstants.ACTION_TOGGLE_PLAY) {
            EventBus.getDefault().postSticky(BusControlClicker(AppConstants.ACTION_TOGGLE_PLAY))
        } else if (intent.action == AppConstants.ACTION_PRIVE) {
            EventBus.getDefault().postSticky(BusControlClicker(AppConstants.ACTION_PRIVE))
        } else if (intent.action == AppConstants.ACTION_STOP) {
            EventBus.getDefault().postSticky(BusControlClicker(AppConstants.ACTION_STOP))
        }
    }
}