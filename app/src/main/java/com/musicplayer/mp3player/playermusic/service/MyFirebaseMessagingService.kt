package com.musicplayer.mp3player.playermusic.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val TAG: String = "FirebaseMess"
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            CustomNotification.createNotification(
                applicationContext,
                remoteMessage.notification?.body.toString()
            )
        }

        remoteMessage.notification?.let {
        }
    }

    override fun onNewToken(token: String) {
    }
}