package com.downloadmp3player.musicdownloader.freemusicdownloader.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.EventControlClicker
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
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