package com.downloadmp3player.musicdownloader.freemusicdownloader.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val TAG: String = "FirebaseMess"
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            CustomNotification.createNotification(
                applicationContext,
                remoteMessage.notification?.body.toString()
            )
        }

        remoteMessage.notification?.let {
            Log.e(
                TAG,
                "Message Notification Title: ${it.title}\n" + "Message Notification Body: ${it.body}"
            )
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }
}