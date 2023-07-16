package com.downloadmp3player.musicdownloader.freemusicdownloader.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.downloadmp3player.musicdownloader.freemusicdownloader.R

object CustomNotification {
    fun createNotification(context: Context, content: String) {
        val CHANNEL_ID = "chanel_notify_abon"
        val notificationId = 888899
        val name: CharSequence = "My Channel"
        val description = "This is my channel Cloud Message"

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.logo)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true)

        val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        builder.setSound(alarmSound)
        builder.setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))

        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val importance: Int = NotificationManager.IMPORTANCE_HIGH
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
                mChannel.description = description
                notificationManager.createNotificationChannel(mChannel)
            }
        }
        notificationManager.notify(notificationId, builder.build())
    }
}