package com.musicplayer.mp3player.defaultmusicplayer.service.player_ext

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem
import com.musicplayer.mp3player.defaultmusicplayer.model.ItemMusicOnline
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.playing.PlayerMusicActivity
import com.musicplayer.mp3player.defaultmusicplayer.utils.AppConstants
import com.musicplayer.mp3player.defaultmusicplayer.utils.AppUtils
import com.musicplayer.mp3player.defaultmusicplayer.utils.ArtworkUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.*
import com.musicplayer.mp3player.defaultmusicplayer.utils.AppConstants.OPEN_FROM_NOTIFICATION


const val NOW_PLAYING_CHANNEL_ID = "nowplaying.downloadmp3.notification"
const val NOW_PLAYING_NOTIFICATION_ID = 866379 // Arbitrary number used to identify our notification

/**
 * A wrapper class for ExoPlayer's PlayerNotificationManager. It sets up the notification shown to
 * the user during audio playback and provides track metadata, such as track title and icon image.
 */
class PlayerNotification(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: NotificationListener
) {
    private var mPlayer: Player? = null
    private val notificationManager: PlayerNotificationManager
    var defaultBitmap: Bitmap =
        AppUtils.getBitmapFromVectorDrawable(context, AppConstants.randomThumb())
    var lastBitmap: Bitmap
    var currentItem: Any? = null

    init {
        lastBitmap = defaultBitmap
        val builder = Builder(
            context,
            NOW_PLAYING_NOTIFICATION_ID,
            NOW_PLAYING_CHANNEL_ID
        )
        with(builder) {
            setMediaDescriptionAdapter(DescriptionAdapter())
            setNotificationListener(notificationListener)
            setChannelNameResourceId(R.string.notification_channel)
            setChannelDescriptionResourceId(R.string.notification_channel_description)
            setCustomActionReceiver(CustomActionReceiver())
        }
        notificationManager = builder.build()
        notificationManager.setMediaSessionToken(sessionToken)
        notificationManager.setSmallIcon(R.drawable.ic_now_playing_notification)
        notificationManager.setUsePlayPauseActions(false)
        notificationManager.setUseFastForwardAction(false)
        notificationManager.setUseRewindAction(false)
        notificationManager.setUseNextAction(false)
        notificationManager.setUsePreviousAction(false)
        notificationManager.setUseStopAction(false)
    }

    private fun createBroadcastIntent(
        action: String, context: Context, instanceId: Int
    ): PendingIntent? {
        val intent = Intent(action).setPackage(context.packageName)
        intent.putExtra(EXTRA_INSTANCE_ID, instanceId)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(
                context,
                instanceId,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getBroadcast(
                context, instanceId, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    inner class CustomActionReceiver : PlayerNotificationManager.CustomActionReceiver {
        override fun createCustomActions(
            context: Context,
            instanceId: Int
        ): MutableMap<String, NotificationCompat.Action> {
            val actions: MutableMap<String, NotificationCompat.Action> = HashMap()
            actions[ACTION_PLAY] = NotificationCompat.Action(
                R.drawable.ic_play_small,
                "Play",
                createBroadcastIntent(ACTION_PLAY, context, instanceId)
            )

            actions[ACTION_PAUSE] = NotificationCompat.Action(
                R.drawable.ic_pause_small,
                "Pause",
                createBroadcastIntent(AppConstants.ACTION_TOGGLE_PLAY, context, instanceId)
            )
            actions[AppConstants.ACTION_PRIVE] = NotificationCompat.Action(
                R.drawable.ic_prive_small,
                "Prive",
                createBroadcastIntent(AppConstants.ACTION_PRIVE, context, instanceId)
            )
            actions[AppConstants.ACTION_NEXT] = NotificationCompat.Action(
                R.drawable.ic_next_small,
                "Next",
                createBroadcastIntent(AppConstants.ACTION_NEXT, context, instanceId)
            )
            actions[AppConstants.ACTION_STOP] = NotificationCompat.Action(
                R.drawable.ic_cancel,
                "Stop",
                createBroadcastIntent(AppConstants.ACTION_STOP, context, instanceId)
            )
            return actions
        }

        override fun getCustomActions(player: Player): MutableList<String> {
            val customActions: MutableList<String> = ArrayList()
            customActions.add(AppConstants.ACTION_PRIVE)
            if (player.playWhenReady) {
                customActions.add(ACTION_PAUSE)
            } else {
                customActions.add(ACTION_PLAY)
            }
            customActions.add(AppConstants.ACTION_NEXT)
            customActions.add(AppConstants.ACTION_STOP)
            return customActions
        }

        override fun onCustomAction(player: Player, action: String, intent: Intent) {
        }
    }

    fun hideNotification() {
        notificationManager.setPlayer(null)
    }

    fun showNotificationForPlayer(player: Player, item: Any?) {
        currentItem = null
        currentItem = item
        notificationManager.setPlayer(player)
        notificationManager.invalidate()
    }

    private inner class DescriptionAdapter :
        MediaDescriptionAdapter {
        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val intentOpenOnlineActivity = Intent(context, PlayerMusicActivity::class.java)
            intentOpenOnlineActivity.putExtra(OPEN_FROM_NOTIFICATION, true)
            var pendingIntent: PendingIntent? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.getActivity(
                        context,
                        0, intentOpenOnlineActivity,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                } else {
                    PendingIntent.getActivity(
                        context,
                        0, intentOpenOnlineActivity,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
            return pendingIntent
        }

        override fun getCurrentContentTitle(player: Player): CharSequence {
            return if (currentItem is ItemMusicOnline) {
                (currentItem as ItemMusicOnline).title ?: context.getString(R.string.app_name)
            } else {
                (currentItem as MusicItem).title.toString()
            }
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return if (currentItem is ItemMusicOnline) {
                context.getString(R.string.app_name)
            } else {
                (currentItem as MusicItem).artist.toString()
            }
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap {
            mPlayer = player
            if (currentItem is ItemMusicOnline) {
                Glide.with(context)
                    .asBitmap()
                    .load((currentItem as ItemMusicOnline).resourceThumb)
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            lastBitmap = defaultBitmap
                            lastBitmap.let { callback.onBitmap(it) }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }

                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            lastBitmap = resource
                            callback.onBitmap(resource)
                        }
                    })
                return lastBitmap
            } else {
                Glide.with(context)
                    .asBitmap()
                    .load(ArtworkUtils.getArtworkFromSongID((currentItem as MusicItem).id))
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            lastBitmap = defaultBitmap
                            lastBitmap.let { callback.onBitmap(it) }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }

                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            lastBitmap = resource
                            callback.onBitmap(resource)
                        }
                    })
                return lastBitmap
            }
        }
    }
}
