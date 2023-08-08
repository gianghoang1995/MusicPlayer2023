package com.musicplayer.mp3player.playermusic.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import com.musicplayer.mp3player.playermusic.eventbus.BusChangeNetwork
import org.greenrobot.eventbus.EventBus

class NetworkChangeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        try {
            Handler(Looper.getMainLooper()).postDelayed({
                if (isOnline(context)) {
                    EventBus.getDefault().postSticky(BusChangeNetwork(true))
                } else {
                    EventBus.getDefault().postSticky(BusChangeNetwork(false))
                }
            },3000)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    private fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            netInfo != null && netInfo.isConnected
        } catch (e: NullPointerException) {
            e.printStackTrace()
            false
        }
    }
}