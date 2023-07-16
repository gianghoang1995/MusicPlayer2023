package com.downloadmp3player.musicdownloader.freemusicdownloader.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
import com.google.android.exoplayer2.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT
import com.google.android.exoplayer2.Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.eventbus.*
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.equalizer.EqualizerDaoDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.equalizer.EqualizerHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.FavoriteSqliteHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.PlaylistSongDaoDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.PlaylistSongSqLiteHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.thumnail.AppDatabase
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.CustomPresetItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.MusicItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.net.utilsonline.GenerateUrlCallback
import com.downloadmp3player.musicdownloader.freemusicdownloader.net.utilsonline.GenerateUrlMusicUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline
import com.downloadmp3player.musicdownloader.freemusicdownloader.sensor.ShakeDetector
import com.downloadmp3player.musicdownloader.freemusicdownloader.service.player_ext.PlayerNotification
import com.downloadmp3player.musicdownloader.freemusicdownloader.service.receiver.BroadcastControl
import com.downloadmp3player.musicdownloader.freemusicdownloader.service.receiver.ScreenActionReceiver
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.playing.PlayerMusicActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.ArtworkUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.PreferenceUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MusicPlayerService : MediaBrowserServiceCompat(),
    GenerateUrlCallback {
    var urlPlayer: String? = null
    var urlLosslessDownload: String? = null
    private var currentOnlineItem: ItemMusicOnline? = null
    private var currentItemSong: MusicItem? = null
    private var parseRunning = false
    private lateinit var screenactionreceiver: ScreenActionReceiver
    var songListSqliteHelper: PlaylistSongSqLiteHelperDB? = null
    var songListDao: PlaylistSongDaoDB? = null
    private val DEFAULTPIORY = 1
    var lstAudio: ArrayList<Any?> = ArrayList()
    var listPreviousOnline: ArrayList<ItemMusicOnline?> = ArrayList()
    var listEqualizer = ArrayList<CustomPresetItem>()

    var isPlayingOnline = false
    var isServiceRunning = false

    var isDurationSet = false
    var songPos = 0
    var equalizerHelper: EqualizerHelperDB? = null
    var equalizerDao: EqualizerDaoDB? = null
    private var mLastShakeTime: Long = 0

    /*Equalizer*/
    var equalizer: Equalizer? = null
    var virtualizer: Virtualizer? = null
    var bassBoost: BassBoost? = null
    var environmentalReverb: PresetReverb? = null
    var stopMusicTimer: CountDownTimer? = null
    var millisUntilFinished: Long = 0
    lateinit var thumbStoreDB: AppDatabase
    var lyrics: String? = null
    var lyricsHtml: String? = null

    var playerNotification: PlayerNotification? = null
    var mediaSession: MediaSessionCompat? = null
    var mediaSessionConnector: MediaSessionConnector? = null
    var mStateBuilder: PlaybackStateCompat.Builder? = null
    lateinit var callback: CustomMediaSeasionCallback

    var bitmapDefault: Bitmap? = null
    private var playerListener: PlayerEventListener = PlayerEventListener()

    var exoPlayer: ExoPlayer? = null
    var generateUrlMusicUtils: GenerateUrlMusicUtils? = null

    lateinit var obverseMusicUtils: ObverseMusicUtils

    private var broadcastControl: BroadcastControl? = null

    private val mBinder: IBinder = MusicServiceBinder()

    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicPlayerService {
            return this@MusicPlayerService
        }
    }

    override fun onCreate() {
        super.onCreate()
        generateUrlMusicUtils = GenerateUrlMusicUtils(this, this)
        thumbStoreDB = AppDatabase(this)
        setBroadcast()

        obverseMusicUtils = ObverseMusicUtils(this)
        callback = CustomMediaSeasionCallback()
        initMediaSession()
        bitmapDefault = AppUtils.getBitmapFromVectorDrawable(this, R.drawable.ic_song)
        EventBus.getDefault().register(this)
        PreferenceUtils.put(AppConstants.IS_SHOW_LOCK, false)
        songListSqliteHelper =
            PlaylistSongSqLiteHelperDB(
                this,
                FavoriteSqliteHelperDB.DEFAULT_FAVORITE
            )
        songListDao =
            PlaylistSongDaoDB(
                songListSqliteHelper
            )

        equalizerHelper =
            EqualizerHelperDB(
                applicationContext
            )
        equalizerDao =
            EqualizerDaoDB(
                equalizerHelper
            )
        getAllPresetEqualizer()
        screenactionreceiver = ScreenActionReceiver()
        registerReceiver(screenactionreceiver, screenactionreceiver.filter)
        if (PreferenceUtils.getValueBoolean(AppConstants.PREF_SHAKE)) {
            initShakeReceiver()
        }
    }

    private fun setBroadcast() {
        broadcastControl = BroadcastControl()
        val filter = IntentFilter()
        filter.addAction(AppConstants.ACTION_NEXT)
        filter.addAction(AppConstants.ACTION_PRIVE)
        filter.addAction(AppConstants.ACTION_TOGGLE_PLAY)
        filter.addAction(AppConstants.ACTION_STOP)
        registerReceiver(broadcastControl, filter)
    }

    private fun initMediaSession() {
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        var pendingItent: PendingIntent? = null
        pendingItent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(
                this,
                0,
                mediaButtonIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getBroadcast(
                this, 0, mediaButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        mediaSession = MediaSessionCompat(this, "MusicStreaming", null, pendingItent)
        mediaSession?.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        mediaSession?.setMediaButtonReceiver(pendingItent)
        mediaSession?.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL)
        mStateBuilder = PlaybackStateCompat.Builder().setActions(
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_PLAY_PAUSE
        ).addCustomAction(
            PlaybackStateCompat.CustomAction.Builder(
                AppConstants.ACTION_PRIVE, "Prive", R.drawable.ic_prive_small
            ).build()
        ).addCustomAction(
            PlaybackStateCompat.CustomAction.Builder(
                AppConstants.ACTION_TOGGLE_PLAY, "Toggle", R.drawable.ic_play_small
            ).build()
        ).addCustomAction(
            PlaybackStateCompat.CustomAction.Builder(
                AppConstants.ACTION_NEXT, "Next", R.drawable.ic_next_small
            ).build()
        )

        mediaSession?.setPlaybackState(mStateBuilder?.build())
        mediaSession?.setCallback(callback)
        sessionToken = mediaSession?.sessionToken
        playerNotification = mediaSession?.sessionToken?.let {
            PlayerNotification(
                this, it, PlayerNotificationListener()
            )
        }

        initExoPlayer()

    }

    fun setHandleAudioBecomeNoise() {
        exoPlayer?.setHandleAudioBecomingNoisy(PreferenceUtils.getValueBoolean(AppConstants.PREF_HEADPHONE))
    }

    private fun initExoPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        val atrs = AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA).build()
        exoPlayer?.setAudioAttributes(atrs, true)
        exoPlayer?.setHandleAudioBecomingNoisy(PreferenceUtils.getValueBoolean(AppConstants.PREF_HEADPHONE))
        exoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
        exoPlayer?.addListener(playerListener)
        mediaSession?.isActive = true
        playerNotification = mediaSession?.sessionToken?.let {
            PlayerNotification(
                this, it, PlayerNotificationListener()
            )
        }
        mediaSessionConnector = MediaSessionConnector(mediaSession!!)
        mediaSessionConnector?.setQueueNavigator(QueueNavigator(mediaSession!!))
        mediaSessionConnector?.setPlayer(exoPlayer)
        mediaSessionConnector?.setDispatchUnsupportedActionsEnabled(true)
        mediaSession?.isActive = true
    }

    private fun initShakeReceiver() {
        offShake()
        ShakeDetector.create(this, object : ShakeDetector.OnShakeListener {
            override fun OnShake() {
                if (SystemClock.elapsedRealtime() - mLastShakeTime < 1000) {
                    return
                } else {
                    next()
                    mLastShakeTime = SystemClock.elapsedRealtime()
                }
            }
        })
        ShakeDetector.updateConfiguration(1f, PreferenceUtils.getShakeCount())
        ShakeDetector.start()
    }

    private fun offShake() {
        ShakeDetector.stop()
        ShakeDetector.destroy()
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onGetRoot(
        clientPackageName: String, clientUid: Int, rootHints: Bundle?
    ): BrowserRoot? {
        return null
    }

    override fun onLoadChildren(
        parentMediaId: String, result: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
    }

    private fun openPlayerActivity(context: Context?) {
        val intent = Intent(context, PlayerMusicActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onDestroy() {
        isServiceRunning = false
        obverseMusicUtils.isServiceRunning.postValue(false)
        offShake()

        mediaSession?.isActive = false

        releaseExoPlayer()

        EventBus.getDefault().unregister(this)
        unregisterReceiver(screenactionreceiver)
        unregisterReceiver(broadcastControl)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        var keepSerrvice = START_NOT_STICKY //1
        val action = intent?.action
        if (intent != null && action != null) {
            when (action) {
                AppConstants.ACTION_SET_DATA_ONLINE -> {
                    val item =
                        intent.getParcelableExtra<ItemMusicOnline>(AppConstants.ACTION_SET_DATA_ONLINE)
                    isServiceRunning = true
                    obverseMusicUtils.isServiceRunning.postValue(true)
                    isPlayingOnline = true
                    currentItemSong = null
                    if (!parseRunning) {
                        currentOnlineItem = item
                        playerNotification?.hideNotification()
                        stopPlayer()
                        obverseMusicUtils.isLoading.postValue(true)
                        obverseMusicUtils.listData.postValue(null)
                        obverseMusicUtils.currentItemAudio.postValue(currentOnlineItem)
                        bitmapDefault =
                            item?.resourceThumb?.let { AppUtils.drawableToBitmap(this, it) }
                        listPreviousOnline.add(item)
                        urlPlayer = null
                        generateUrlMusicUtils?.getUrlAudio(item)
                    } else {
                        Toast.makeText(this, getString(R.string.please_wait), Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                AppConstants.ACTION_SET_DATA_PLAYER -> {
                    isPlayingOnline = false
                    isDurationSet = false
                    isServiceRunning = true
                    obverseMusicUtils.isServiceRunning.postValue(true)
                    setDataSource()
                }
                AppConstants.ACTION_TOGGLE_PLAY -> {
                    togglePlay()
                }
                AppConstants.ACTION_RESTART -> {
                    restart()
                }
                AppConstants.ACTION_NEXT -> {
                    next()
                }
                AppConstants.ACTION_PRIVE -> {
                    prive()
                }
                AppConstants.EQUALIZER_STATUS -> {
                    enableEqualizer()
                }
                AppConstants.ACTION_CHANGE_PRESET_EQUALIZER -> {
                    val presetNumber = intent.getIntExtra(
                        AppConstants.ACTION_CHANGE_PRESET_EQUALIZER, 0
                    )
                    changePresetEqualizer(presetNumber)
                }
                AppConstants.EQUALIZER_SLIDER_CHANGE -> {
                    val band = intent.getIntExtra(AppConstants.EQUALIZER_SLIDER_CHANGE, 0)
                    val progress = intent.getIntExtra(AppConstants.EQUALIZER_SLIDER_VALUE, 0)
                    changeSliderEqualizer(band, progress)
                }
                AppConstants.VIRTUALIZER_STATUS -> {
                    enableVirtuarl()
                }
                AppConstants.VIRTUALIZER_STRENGTH -> {
                    val presetNumber = intent.getIntExtra(AppConstants.VIRTUALIZER_STRENGTH, 0)
                    setStrengthVirtuarl(presetNumber)
                }
                AppConstants.BASSBOSSTER_STATUS -> {
                    enableBassBoster()
                }
                AppConstants.BASSBOSSTER_STRENGTH -> {
                    val presetNumber = intent.getIntExtra(AppConstants.BASSBOSSTER_STRENGTH, 0)
                    setStrengthBassBoster(presetNumber)
                }
                AppConstants.SET_TIMER -> {
                    if (isServiceRunning) {
                        val timeText = intent.getStringExtra(AppConstants.TIMER_TEXT)

                        val millis = intent.getLongExtra(AppConstants.SET_TIMER, 0)
                        if (millis != 0L) {
                            setTimmer(millis)
                            Toast.makeText(
                                this,
                                "${getString(R.string.music_has_stop)}\n" + "$timeText",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            stopMusicTimer?.cancel()
                            obverseMusicUtils.isRunningTimer.postValue(false)
                            if (isPlaying()) {
                                togglePlay()
                            }
                        }

                    } else {
                        showMessage(getString(R.string.no_playlist_play))
                    }
                }
                AppConstants.ACTION_SHAKE -> {
                    if (PreferenceUtils.getValueBoolean(AppConstants.PREF_SHAKE)) {
                        initShakeReceiver()
                    } else {
                        offShake()
                    }
                }
                AppConstants.ACTION_STOP -> {
                    keepSerrvice = START_NOT_STICKY
                    stopServiceAndCloseNotification()
                }
            }
        }
        return keepSerrvice
    }

    private fun restart() {
        if (isServiceRunning) {
            if (exoPlayer != null) {
                exoPlayer?.seekTo(0)
                exoPlayer?.playWhenReady = true
            }
        }
    }

    private fun enableAllAudioEffect() {
        enableEqualizer()
        enableBassBoster()
        enableVirtuarl()
    }

    private fun disableAllAudioEffect() {
        if (environmentalReverb != null) {
            environmentalReverb?.release()
            environmentalReverb = null
        }

        if (virtualizer != null) {
            virtualizer!!.release()
            virtualizer = null
        }

        if (bassBoost != null) {
            bassBoost!!.release()
            bassBoost = null
        }

        if (equalizer != null) {
            equalizer!!.release()
            equalizer = null
        }
    }

    private fun changeSliderEqualizer(colum: Int, progress: Int) {
        if (PreferenceUtils.getValueBoolean(AppConstants.EQUALIZER_STATUS)) {
            if (equalizer != null) {
                equalizer?.setBandLevel(colum.toShort(), progress.toShort())
            }
        }
    }

    private fun enableVirtuarl() {
        if (PreferenceUtils.getValueBoolean(AppConstants.VIRTUALIZER_STATUS)) {
            try {
                virtualizer = exoPlayer?.audioSessionId?.let { Virtualizer(DEFAULTPIORY, it) }
                virtualizer?.enabled = true
                virtualizer?.setStrength(
                    PreferenceUtils.getValueInt(AppConstants.VIRTUALIZER_STRENGTH).toShort()
                )
            } catch (ex: java.lang.Exception) {
            }
        } else {
            if (virtualizer != null) {
                virtualizer!!.release()
                virtualizer = null
            }
        }
    }

    private fun setStrengthVirtuarl(strength: Int) {
        if (PreferenceUtils.getValueBoolean(AppConstants.VIRTUALIZER_STATUS)) {
            if (virtualizer == null) {
                try {
                    virtualizer = exoPlayer?.audioSessionId?.let { Virtualizer(DEFAULTPIORY, it) }
                    virtualizer?.enabled = true
                    virtualizer?.setStrength(strength.toShort())
                } catch (ex: java.lang.Exception) {
                }
            } else {
                try {
                    virtualizer?.setStrength(strength.toShort())
                } catch (ex: java.lang.Exception) {
                }
            }
        }
    }

    private fun enableBassBoster() {
        if (PreferenceUtils.getValueBoolean(AppConstants.BASSBOSSTER_STATUS)) {
            try {
                bassBoost = exoPlayer?.audioSessionId?.let { BassBoost(DEFAULTPIORY, it) }
                bassBoost?.enabled = true
                bassBoost?.setStrength(
                    PreferenceUtils.getValueInt(AppConstants.BASSBOSSTER_STRENGTH).toShort()
                )
            } catch (ex: java.lang.Exception) {
            }
        } else {
            if (bassBoost != null) {
                bassBoost!!.release()
                bassBoost = null
            }
        }
    }

    private fun setStrengthBassBoster(strength: Int) {
        if (PreferenceUtils.getValueBoolean(AppConstants.BASSBOSSTER_STATUS)) {
            if (bassBoost == null) {
                try {
                    bassBoost = exoPlayer?.audioSessionId?.let { BassBoost(DEFAULTPIORY, it) }
                    bassBoost?.enabled = true
                    bassBoost?.setStrength(strength.toShort())
                } catch (ex: java.lang.Exception) {
                }
            } else {
                try {
                    bassBoost?.setStrength(strength.toShort())
                } catch (ex: java.lang.Exception) {
                }
            }
        }
    }

    private fun enableEqualizer() {
        if (PreferenceUtils.getValueBoolean(AppConstants.EQUALIZER_STATUS)) {
            if (equalizer != null) {
                equalizer?.release()
                equalizer = null
            }
            try {
                equalizer = exoPlayer?.audioSessionId?.let { Equalizer(DEFAULTPIORY, it) }
                equalizer?.properties =
                    Equalizer.Settings("Equalizer;curPreset=-1;numBands=5;band1Level=0;band2Level=0;band3Level=0;band4Level=0;band5Level=0;")
                equalizer?.enabled = true
                changePresetEqualizer(PreferenceUtils.getValueInt(AppConstants.PRESET_NUMBER))
            } catch (ex: java.lang.Exception) {
            }
        } else {
            if (equalizer != null) {
                equalizer!!.release()
                equalizer = null
            }
        }
    }

    private fun getAllPresetEqualizer() {
        listEqualizer.clear()
        equalizerDao?.allPreset?.let { listEqualizer.addAll(it) }
    }

    private fun changePresetEqualizer(pos: Int) {
        getAllPresetEqualizer()
        if (PreferenceUtils.getValueBoolean(AppConstants.EQUALIZER_STATUS)) {
            if (equalizer == null) {
                try {
                    equalizer = exoPlayer?.audioSessionId?.let { Equalizer(DEFAULTPIORY, it) }
                    equalizer?.properties =
                        Equalizer.Settings("Equalizer;curPreset=-1;numBands=5;band1Level=0;band2Level=0;band3Level=0;band4Level=0;band5Level=0;")
                    equalizer?.enabled = true
                } catch (ex: java.lang.Exception) {
                }
            }

            if (pos < 10) {
                equalizer?.usePreset(pos.toShort())
            } else {
                val customPreset: CustomPresetItem = listEqualizer.get(pos)
                equalizer?.setBandLevel(0, customPreset.slider1.toShort())
                equalizer?.setBandLevel(1, customPreset.slider2.toShort())
                equalizer?.setBandLevel(2, customPreset.slider3.toShort())
                equalizer?.setBandLevel(3, customPreset.slider4.toShort())
                equalizer?.setBandLevel(4, customPreset.slider5.toShort())
            }
        } else {
            if (equalizer != null) {
                equalizer!!.release()
                equalizer = null
            }
        }
    }

    fun updateLyricData(lyric: String?, html: String?) {
        lyrics = lyric
        lyricsHtml = html
    }

    private fun setTimmer(millis: Long) {
        millisUntilFinished = millis
        if (stopMusicTimer != null) {
            stopMusicTimer?.cancel()
            obverseMusicUtils.isRunningTimer.postValue(false)
        }

        if (millis.toInt() > 0) {
            stopMusicTimer = object : CountDownTimer(millis, 1000) {
                override fun onFinish() {
                    stopMusicTimer = null
                    if (isPlaying()) {
                        togglePlay()
                    }
                }

                override fun onTick(utilFinish: Long) {
                    millisUntilFinished = utilFinish
                }
            }
            stopMusicTimer?.start()
            obverseMusicUtils.isRunningTimer.postValue(true)
        }
    }

    private fun stopServiceAndCloseNotification() {
        lstAudio.clear()
        if (stopMusicTimer != null) {
            stopMusicTimer?.cancel()
            obverseMusicUtils.isRunningTimer.postValue(false)
            stopMusicTimer = null
        }
        millisUntilFinished = 0
        mediaSession?.run {
            isActive = false
        }
        releaseExoPlayer()
        disableAllAudioEffect()
        obverseMusicUtils.resetObverse()
        currentItemSong = null
        currentOnlineItem = null
        isServiceRunning = false
        playerNotification?.hideNotification()
        EventBus.getDefault().postSticky(EventStopService(true))
        stopSelf()
    }

    private fun releaseExoPlayer() {
        if (exoPlayer != null) {
            exoPlayer?.pause()
            exoPlayer?.stop()
            exoPlayer?.removeListener(playerListener)
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    fun getCurrentItem(): Any? {
        return if (isPlayingOnline) {
            currentOnlineItem
        } else {
            if (lstAudio.isEmpty())
                null
            else
                currentItemSong ?: lstAudio[songPos] as MusicItem
        }
    }

    fun getCurrentSong(): MusicItem? {
        if (isPlayingOnline) {
            return null
        }
        return (getCurrentItem() as MusicItem?)
    }

    fun getCurrentOnline(): ItemMusicOnline? {
        return (getCurrentItem() as ItemMusicOnline?)
    }

    private fun showNotification() {
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.action = AppConstants.ACTION_SET_DATA_PLAYER
        startService(intent)
    }

    private fun stopServiceIntent() {
        isServiceRunning = false
        obverseMusicUtils.isServiceRunning.postValue(false)
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.action = AppConstants.ACTION_STOP
        startService(intent)
    }

    private fun stopPlayer() {
        if (exoPlayer != null) {
            exoPlayer?.stop()
            obverseMusicUtils._exoPlayer.postValue(null)
        }
        isDurationSet = false
    }

    fun seek(position: Int) {
        if (exoPlayer != null) {
            exoPlayer?.seekTo(position.toLong())
        }
    }

    fun next() {
        if (isPlayingOnline) {
            isDurationSet = false
            if (lstAudio.isNotEmpty()) {
                if (PreferenceUtils.getValueInt(AppConstants.LOOP_MODE) == Player.REPEAT_MODE_ONE) {
                    if (exoPlayer != null) {
                        exoPlayer?.seekTo(0)
                        if (exoPlayer?.playWhenReady == false) {
                            togglePlay()
                        }
                    }
                } else {
                    if (!parseRunning) {
                        addLastItemToListPrevious()
                        currentOnlineItem = lstAudio[0] as ItemMusicOnline
                        obverseMusicUtils.currentItemAudio.postValue(getCurrentItem())
                        obverseMusicUtils.listData.postValue(null)
                        obverseMusicUtils.isLoading.postValue(true)
                        bitmapDefault = currentOnlineItem?.resourceThumb?.let {
                            AppUtils.drawableToBitmap(
                                this, it
                            )
                        }
                        generateUrlMusicUtils?.getUrlAudio(currentOnlineItem)
                        parseRunning = true
                        urlPlayer = null
                    } else {
                        Toast.makeText(this, getString(R.string.please_wait), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        } else {
            isDurationSet = false
            if (lstAudio.isEmpty()) {
                getString(R.string.no_playlist_play)
            } else {
                if (PreferenceUtils.getValueInt(AppConstants.LOOP_MODE) == Player.REPEAT_MODE_ONE) {
                    if (exoPlayer != null) {
                        exoPlayer?.seekTo(0)
                        if (exoPlayer?.playWhenReady == false) {
                            togglePlay()
                        }
                    }
                } else {
                    if (PreferenceUtils.getValueInt(AppConstants.LOOP_MODE) == AppConstants.LOOP.LOOP_SHUFFLE) {
                        songPos = AppUtils.getRandomNumber(lstAudio.size - 1)
                    } else {
                        if (songPos == lstAudio.size - 1) {
                            songPos = 0
                        } else
                            songPos++
                    }
                    setDataSource()
                    obverseMusicUtils.isLoading.postValue(false)
                    if (exoPlayer?.playWhenReady == false) {
                        exoPlayer?.playWhenReady = true
                    }
                }
            }
            EventBus.getDefault().postSticky(EventCloseDialog(true))
        }
        obverseMusicUtils.isEndSong.postValue(true)
    }

    private fun prive() {
        if (isPlayingOnline) {
            if (PreferenceUtils.getValueInt(AppConstants.LOOP_MODE) == Player.REPEAT_MODE_ONE) {
                if (exoPlayer != null) {
                    exoPlayer?.seekTo(0)
                    if (!isPlaying()) {
                        togglePlay()
                    }
                }
            } else {
                stopPlayer()
                if (listPreviousOnline.isNotEmpty()) {
                    if (!parseRunning) {
                        if (listPreviousOnline.size != 1) {
                            currentOnlineItem = listPreviousOnline[listPreviousOnline.size - 1]
                            obverseMusicUtils.currentItemAudio.postValue(getCurrentItem())
                            obverseMusicUtils.listData.postValue(null)
                            obverseMusicUtils.isLoading.postValue(true)
                            bitmapDefault = currentOnlineItem?.resourceThumb?.let {
                                AppUtils.drawableToBitmap(
                                    this, it
                                )
                            }
                            listPreviousOnline.removeAt(listPreviousOnline.size - 1)
                        } else {
                            currentOnlineItem = listPreviousOnline[0]
                            obverseMusicUtils.currentItemAudio.postValue(getCurrentItem())
                            obverseMusicUtils.isLoading.postValue(true)
                            bitmapDefault = currentOnlineItem?.resourceThumb?.let {
                                AppUtils.drawableToBitmap(
                                    this, it
                                )
                            }
                        }
                        generateUrlMusicUtils?.getUrlAudio(currentOnlineItem)
                        parseRunning = true
                        urlPlayer = null
                    } else {
                        Toast.makeText(this, getString(R.string.please_wait), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        } else {
            if (lstAudio.isEmpty()) {
                getString(R.string.no_playlist_play)
            } else {
                if (PreferenceUtils.getValueInt(AppConstants.LOOP_MODE) == Player.REPEAT_MODE_ONE) {
                    if (exoPlayer != null) {
                        exoPlayer?.seekTo(0)
                        if (exoPlayer?.playWhenReady == false) {
                            togglePlay()
                        }
                    }
                } else {
                    if (PreferenceUtils.getValueInt(AppConstants.LOOP_MODE) == AppConstants.LOOP.LOOP_SHUFFLE) {
                        songPos = AppUtils.getRandomNumber(lstAudio.size - 1)
                    } else {
                        if (songPos == 0) {
                            songPos = lstAudio.size - 1
                        } else
                            songPos--
                    }
                    setDataSource()
                    obverseMusicUtils.isLoading.postValue(false)
                    if (exoPlayer?.playWhenReady == false) {
                        exoPlayer?.playWhenReady = true
                    }
                }
            }
            EventBus.getDefault().postSticky(EventCloseDialog(true))
        }
        obverseMusicUtils.isEndSong.postValue(true)
    }

    private fun addLastItemToListPrevious() {
        if (currentOnlineItem != null) {
            if (listPreviousOnline.isNotEmpty()) {
                if (listPreviousOnline[listPreviousOnline.size - 1]!!.urlVideo != currentOnlineItem?.urlVideo) {
                    listPreviousOnline.add(currentOnlineItem)
                }
            }
        }
    }

    fun nextSongUnLimited() {
        obverseMusicUtils.isEndSong.postValue(true)
        if (!parseRunning) {
            addLastItemToListPrevious()
            currentOnlineItem = lstAudio[0] as ItemMusicOnline
            obverseMusicUtils.currentItemAudio.postValue(getCurrentItem())
            obverseMusicUtils.listData.postValue(null)
            obverseMusicUtils.isLoading.postValue(true)
            generateUrlMusicUtils?.getUrlAudio(currentOnlineItem)
            parseRunning = true
            urlPlayer = null
        } else {
            Toast.makeText(this, getString(R.string.please_wait), Toast.LENGTH_SHORT).show()
        }
    }

    fun togglePlay() {
        if (exoPlayer != null) {
            if (isPlaying()) {
                exoPlayer?.pause()
                obverseMusicUtils.playbackState.postValue(AppConstants.PLAYBACK_STATE.STATE_PAUSED)
            } else {
                exoPlayer?.play()
                obverseMusicUtils.playbackState.postValue(AppConstants.PLAYBACK_STATE.STATE_PLAYING)
            }
        }
    }

    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying ?: false
    }

    private fun initPrepare() {
        mediaSession?.setCallback(callback)
        mediaSessionConnector?.invalidateMediaSessionMetadata()
        mediaSessionConnector?.invalidateMediaSessionQueue()
        mediaSessionConnector?.invalidateMediaSessionPlaybackState()
    }

    fun setDataSource(url: String?) {
        stopPlayer()
        if (exoPlayer == null) {
            initExoPlayer()
        }
        addSongToHistoryDb()
        val mediaItem = url?.let { MediaItem.fromUri(it) }
        mediaItem?.let { exoPlayer?.setMediaItem(it) }
        exoPlayer?.playWhenReady = true
        exoPlayer?.prepare()
    }

    fun setDataSource() {
        stopPlayer()
        if (exoPlayer == null) {
            initExoPlayer()
        }
        addSongToHistoryDb()
        currentItemSong = lstAudio[songPos] as MusicItem
        val uri = Uri.parse(getCurrentSong()?.songPath)
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer?.setMediaItem(mediaItem)
        obverseMusicUtils.currentItemAudio.postValue(getCurrentItem())
        exoPlayer?.playWhenReady = true
        exoPlayer?.prepare()
    }

    private fun addSongToHistoryDb() {
        if (isPlayingOnline) {
            songListSqliteHelper =
                PlaylistSongSqLiteHelperDB(
                    this,
                    FavoriteSqliteHelperDB.TABLE_LAST_PLAYING
                )
            songListDao =
                PlaylistSongDaoDB(
                    songListSqliteHelper
                )
            songListDao?.insertItemOnlineToPlaylist(getCurrentOnline())
            obverseMusicUtils.insertRecentlyPlayed.postValue(true)
        } else {
            songListSqliteHelper =
                PlaylistSongSqLiteHelperDB(
                    this,
                    FavoriteSqliteHelperDB.TABLE_LAST_PLAYING
                )
            songListDao =
                PlaylistSongDaoDB(
                    songListSqliteHelper
                )
            songListDao?.insertToLocalPlaylist(getCurrentSong())

            songListSqliteHelper =
                PlaylistSongSqLiteHelperDB(
                    this,
                    FavoriteSqliteHelperDB.TABLE_MOST_PLAYING
                )
            songListDao =
                PlaylistSongDaoDB(
                    songListSqliteHelper
                )
            songListDao?.insertSongToMostPlayingLocal(getCurrentSong())
        }
    }

    fun setListSong(context: Context, list: ArrayList<MusicItem>, position: Int) {
        isPlayingOnline = false
        stopPlayer()
        songPos = position
        lstAudio.clear()
        lstAudio.addAll(list)
        obverseMusicUtils.listData.postValue(lstAudio)
        setDataSource()
        showNotification()
        openPlayerActivity(context)
    }

    fun shuffleListSong(lstSong: ArrayList<MusicItem>, context: Context?) {
        isPlayingOnline = false
        stopPlayer()
        lstAudio.clear()
        lstAudio.addAll(lstSong)
        songPos = AppUtils.getRanDom(lstAudio.size - 1)
        obverseMusicUtils.listData.postValue(lstAudio)
        PreferenceUtils.putLoopMode(AppConstants.LOOP.LOOP_SHUFFLE)
        setDataSource()
        showNotification()
        openPlayerActivity(context)
    }

    fun insertNextTrack(song: MusicItem) {
        if (!isPlayingOnline) {
            if (lstAudio.size > 0) {
                lstAudio.addAll(songPos + 1, listOf(song))
                obverseMusicUtils.listData.postValue(lstAudio)
                getString(R.string.txt_done)
            } else {
                setListSong(applicationContext, arrayListOf(song), 0)
            }
        } else {
            showMessage(getString(R.string.you_in_online_mode))
        }
    }

    fun insertListNextTrack(lstSong: ArrayList<MusicItem>) {
        if (!isPlayingOnline) {
            if (lstAudio.size > 0) {
                lstAudio.addAll(songPos + 1, lstSong)
                obverseMusicUtils.listData.postValue(lstAudio)
                getString(R.string.txt_done)
            } else {
                setListSong(applicationContext, lstSong, 0)
            }
        } else {
            showMessage(getString(R.string.you_in_online_mode))
        }
    }

    fun addToQueue(lstSong: ArrayList<MusicItem>) {
        if (!isPlayingOnline) {
            if (lstAudio.size > 0) {
                lstAudio.addAll(lstSong)
                obverseMusicUtils.listData.postValue(lstAudio)
                getString(R.string.txt_done)
            } else {
                setListSong(applicationContext, lstSong, 0)
            }
        } else {
            showMessage(getString(R.string.you_in_online_mode))
        }
    }

    fun moveSongItem(lastPos: Int, moveToPos: Int) {
        val moveItem: Any? = lstAudio[lastPos]
        lstAudio.removeAt(lastPos)
        lstAudio.add(moveToPos, moveItem)
        songPos = lstAudio.indexOf(getCurrentItem())
        obverseMusicUtils.listData.postValue(lstAudio)
    }

    fun deleteSongNowPlaying(item: Any, i: Int) {
        lstAudio.remove(item)
        songPos = lstAudio.indexOf(getCurrentItem())
        obverseMusicUtils.listData.postValue(lstAudio)
    }

    private fun showMessage(res: Int) {
        Toast.makeText(this, getString(res), Toast.LENGTH_LONG).show()
    }

    private fun showMessage(valu: String) {
        Toast.makeText(this, valu, Toast.LENGTH_LONG).show()
    }

    fun checkFavorite(): Boolean {
        return if (isPlayingOnline) {
            songListSqliteHelper =
                PlaylistSongSqLiteHelperDB(
                    this,
                    FavoriteSqliteHelperDB.DEFAULT_FAVORITE
                )
            songListDao =
                PlaylistSongDaoDB(
                    songListSqliteHelper
                )
            if (getCurrentOnline() != null) songListDao?.isContainsItemOnline(getCurrentOnline()) != null
            else false
        } else {
            songListSqliteHelper =
                PlaylistSongSqLiteHelperDB(
                    this,
                    FavoriteSqliteHelperDB.DEFAULT_FAVORITE
                )
            songListDao =
                PlaylistSongDaoDB(
                    songListSqliteHelper
                )
            if (getCurrentSong() != null) songListDao?.isContainsItemLocal(getCurrentSong()) != null
            else false
        }
    }

    inner class QueueNavigator(
        mediaSession: MediaSessionCompat
    ) : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(
            player: Player, windowIndex: Int
        ): MediaDescriptionCompat {
            if (isPlayingOnline) {
                if (getCurrentItem() != null) return MediaDescriptionCompat.Builder()
                    .setTitle(getCurrentOnline()?.title).setSubtitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_name)).setIconBitmap(bitmapDefault)
                    .build()

            } else {
                val uriArt = (getCurrentItem() as MusicItem?)?.id?.let {
                    ArtworkUtils.getArtworkFromSongID(
                        it
                    )
                }
                return MediaDescriptionCompat.Builder().setTitle(getCurrentSong()?.title)
                    .setSubtitle(getCurrentSong()?.artist)
                    .setDescription(getCurrentSong()?.album.toString()).setIconUri(uriArt).build()

            }
            return MediaDescriptionCompat.Builder().setTitle(getString(R.string.app_name))
                .setSubtitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_name)).setIconBitmap(bitmapDefault).build()
        }
    }

    inner class PlayerNotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int, notification: Notification, ongoing: Boolean
        ) {
            if (ongoing) {
                try {
                    ContextCompat.startForegroundService(
                        applicationContext,
                        Intent(applicationContext, this@MusicPlayerService.javaClass)
                    )
                    startForeground(notificationId, notification)
                    isServiceRunning = true
                    obverseMusicUtils.isServiceRunning.postValue(true)
                } catch (ex: java.lang.Exception) {
                }
            } else {
                stopForeground(false)
            }
        }

        override fun onNotificationCancelled(
            notificationId: Int, dismissedByUser: Boolean
        ) {
            stopForeground(true)
//            stopServiceIntent()
        }
    }

    /**
     * Listen for events from ExoPlayer.
     */
    inner class PlayerEventListener : Player.Listener {
        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            if (playWhenReady) {
                obverseMusicUtils.playbackState.postValue(AppConstants.PLAYBACK_STATE.STATE_PLAYING)
            } else {
                obverseMusicUtils.playbackState.postValue(AppConstants.PLAYBACK_STATE.STATE_PAUSED)
            }

            if (reason == PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM) {
                isDurationSet = false
                if (PreferenceUtils.getLoopMode() == Player.REPEAT_MODE_ONE) {
                    restart()
                } else {
                    next()
                }
            }

            if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                isDurationSet = false
                if (PreferenceUtils.getLoopMode() == Player.REPEAT_MODE_ONE) {
                    restart()
                } else {
                    next()
                }
            }
            super.onPlayWhenReadyChanged(playWhenReady, reason)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    obverseMusicUtils.isLoading.postValue(true)
                }

                Player.STATE_READY -> {
                    obverseMusicUtils._exoPlayer.postValue(exoPlayer)
                    obverseMusicUtils.isLoading.postValue(false)
                    if (!isDurationSet) {
                        initPrepare()
                        exoPlayer?.let {
                            playerNotification?.showNotificationForPlayer(
                                it, getCurrentItem()
                            )
                        }
                        enableAllAudioEffect()
                        isDurationSet = true
                    }
                }

                Player.STATE_ENDED -> {
                    Log.e("End", "STATE_END")
                    obverseMusicUtils.isEndSong.postValue(true)
                    isDurationSet = false
                    next()
                }

                Player.STATE_IDLE -> {
                }
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            /*          if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) || events.contains(Player.EVENT_MEDIA_METADATA_CHANGED) || events.contains(
                              Player.EVENT_IS_PLAYING_CHANGED
                          ) || events.contains(Player.EVENT_PLAYBACK_SUPPRESSION_REASON_CHANGED)
                      ) {


                          for (i in 0 until events.size()) {
                              Log.e("Event1", events.get(i).toString())
                          }

                      }*/

            if (events.contains(Player.EVENT_PLAYBACK_SUPPRESSION_REASON_CHANGED)) {
                obverseMusicUtils.playbackState.postValue(AppConstants.PLAYBACK_STATE.STATE_PAUSED)
            }

            if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                if (isPlaying()) {
                    obverseMusicUtils.playbackState.postValue(AppConstants.PLAYBACK_STATE.STATE_PLAYING)
                } else {
                    obverseMusicUtils.playbackState.postValue(AppConstants.PLAYBACK_STATE.STATE_PAUSED)
                }
            }


            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) || events.contains(Player.EVENT_TIMELINE_CHANGED) || events.contains(
                    Player.EVENT_POSITION_DISCONTINUITY
                )
            ) {
                for (i in 0 until events.size()) {
//                    Log.e("Event2", events.get(i).toString())
                }
//                if (!isPlaying()) {
//                    togglePlay()
//                }
            }

        }

        override fun onPlayerError(error: PlaybackException) {
            obverseMusicUtils.isLoading.postValue(false)
            if (isPlayingOnline) {
                stopServiceAndCloseNotification()
                when (error.errorCode) {
                    ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                        showMessage(getString(R.string.please_check_internet_connection))
                    }
                    ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> {
                        showMessage(getString(R.string.error_please_try_again))
                    }
                }

            } else {
                next()
                exoPlayer?.prepare()
            }
        }
    }

    override fun onGenerateUrlSuccess(
        url: String?,
        urlLossless: String?,
        lstRecomend: ArrayList<ItemMusicOnline>?
    ) {
        obverseMusicUtils.isLoading.postValue(false)
        urlPlayer = url
        urlLosslessDownload = urlLossless
        setDataSource(urlPlayer)
        parseRunning = false
        if (lstRecomend?.isNotEmpty() == true) {
            lstAudio.clear()
            lstAudio.addAll(lstRecomend.shuffled())
            obverseMusicUtils.listData.postValue(lstAudio)
        }
    }

    override fun onGenerateUrlError() {
        parseRunning = false
        urlPlayer = null
        Toast.makeText(
            applicationContext, getString(R.string.please_try_again), Toast.LENGTH_SHORT
        ).show()
        obverseMusicUtils.resetObverse()
        stopServiceAndCloseNotification()
    }

    inner class CustomMediaSeasionCallback : MediaSessionCompat.Callback() {
        override fun onCommand(command: String, extras: Bundle, cb: ResultReceiver) {
            super.onCommand(command, extras, cb)
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
            return super.onMediaButtonEvent(mediaButtonEvent)
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            exoPlayer?.seekTo(pos)
        }

        override fun onPlay() {
            super.onPlay()
            togglePlay()
        }

        override fun onPause() {
            super.onPause()
            togglePlay()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            next()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            prive()
        }

        override fun onCustomAction(action: String, extras: Bundle) {
            super.onCustomAction(action, extras)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun ConTrolBroadcast(control: EventControlClicker) {
        val intent = control.action
        when (intent) {
            AppConstants.ACTION_NEXT -> if (isServiceRunning) next()
            AppConstants.ACTION_TOGGLE_PLAY -> if (isServiceRunning) togglePlay()
            AppConstants.ACTION_PRIVE -> if (isServiceRunning) prive()
            AppConstants.ACTION_STOP -> stopServiceIntent()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDeleteSong(item: EventDeleteSong) {
        lstAudio.remove(item.song)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun eventScreen(event: EventShowLockScreen) {
        if (!(application as BaseApplication).isLockScreenRunning && PreferenceUtils.getValueBoolean(
                AppConstants.PREF_LOCK_SCREEN, false
            ) && isPlaying()
        ) {
//            val intent = Intent(this, LookScreenActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
        }
    }

}