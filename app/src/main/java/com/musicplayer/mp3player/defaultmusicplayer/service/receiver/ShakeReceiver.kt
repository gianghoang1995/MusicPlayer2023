package com.musicplayer.mp3player.defaultmusicplayer.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ShakeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (null != intent && intent.action.equals("shake.detector")) {
            Log.e("SHAKE", "Shake nè")
        }
    }
}