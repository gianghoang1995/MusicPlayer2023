package com.musicplayer.mp3player.defaultmusicplayer.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.*
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R
import com.musicplayer.mp3player.defaultmusicplayer.database.equalizer.EqualizerDaoDB
import com.musicplayer.mp3player.defaultmusicplayer.database.equalizer.EqualizerHelperDB
import com.musicplayer.mp3player.defaultmusicplayer.database.playlist.FavoriteDaoDB
import com.musicplayer.mp3player.defaultmusicplayer.database.playlist.FavoriteSqliteHelperDB
import com.musicplayer.mp3player.defaultmusicplayer.model.CustomPresetItem
import com.musicplayer.mp3player.defaultmusicplayer.service.receiver.NetworkChangeReceiver
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.plashscreen.PlashScreenActivity
import com.musicplayer.mp3player.defaultmusicplayer.utils.AppConstants.testDevices
import com.musicplayer.mp3player.defaultmusicplayer.utils.AppUtils
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.BuildConfig
import com.musicplayer.mp3player.defaultmusicplayer.service.ObverseDownloadServiceUtils
import com.google.android.gms.ads.*
import com.utils.adsloader.AppOpenAdManager
import com.utils.adsloader.InterstitialPreloadAdManager
import com.utils.adsloader.InterstitialSingleReqAdManager
import com.utils.adsloader.utils.DialogLoadingOpenAds
import java.util.*


class BaseApplication : Application(), LifecycleObserver, Application.ActivityLifecycleCallbacks {
    lateinit var equalizerHelper: EqualizerHelperDB
    lateinit var equalizerDao: EqualizerDaoDB
    lateinit var favoriteSqliteHelper: FavoriteSqliteHelperDB
    lateinit var favoriteDao: FavoriteDaoDB
    var isLockScreenRunning = false
    var bmImg: Bitmap? = null
    var appOpenAdManager: AppOpenAdManager? = null
    var adsFullPlash: InterstitialPreloadAdManager? = null
    var adsFullInApp: InterstitialSingleReqAdManager? = null
    var adsFullOptionMenu: InterstitialSingleReqAdManager? = null
    var currentActivity: Activity? = null
    var obverseDownloadServiceUtils: ObverseDownloadServiceUtils? = null
    var networkChangeReceiver: NetworkChangeReceiver? = null
    var currentKeyLang = "en"

    companion object {
        @SuppressLint("StaticFieldLeak")
        var mInstance: BaseApplication? = null
        var isShowAdsFull = false
        fun getAppInstance(): BaseApplication {
            return mInstance as BaseApplication
        }
    }

    override fun onCreate() {
        super.onCreate()
        currentKeyLang = Locale.getDefault().language
        networkChangeReceiver = NetworkChangeReceiver()
        registerReceiver(
            networkChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
        obverseDownloadServiceUtils = ObverseDownloadServiceUtils(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        mInstance = this
        equalizerHelper = EqualizerHelperDB(this)
        equalizerDao = EqualizerDaoDB(
            equalizerHelper
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName()
            val packageName = this.packageName
            if (packageName != processName) {
                WebView.setDataDirectorySuffix(processName)
            }
        }

        MobileAds.initialize(this) {
            MobileAds.setAppMuted(true)
            val requestConfiguration =
                RequestConfiguration.Builder().setTestDeviceIds(testDevices()).build()
            MobileAds.setRequestConfiguration(requestConfiguration)
        }
        initApplovinMediation()
        initAds()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        registerActivityLifecycleCallbacks(this)

        favoriteSqliteHelper = FavoriteSqliteHelperDB(
            this
        )
        favoriteDao = FavoriteDaoDB(
            this, favoriteSqliteHelper
        )
        favoriteDao.insertFavorite(FavoriteSqliteHelperDB.DEFAULT_FAVORITE)
        favoriteDao.insertFavorite(FavoriteSqliteHelperDB.TABLE_RECENT_ADDED)
        favoriteDao.insertFavorite(FavoriteSqliteHelperDB.TABLE_LAST_PLAYING)
        favoriteDao.insertFavorite(FavoriteSqliteHelperDB.TABLE_MOST_PLAYING)
        addDefaultEqualizer(this, equalizerDao)
        refreshBitmapBackground()
    }

    private fun initAds() {
        appOpenAdManager = AppOpenAdManager(
            this,
            BuildConfig.app_open_ads
        )

        adsFullPlash = InterstitialPreloadAdManager(
            this,
            BuildConfig.full_plash_01,
            BuildConfig.full_plash_02
        )

        adsFullInApp = InterstitialSingleReqAdManager(
            this,
            BuildConfig.full_in_app_01,
            BuildConfig.full_in_app_02
        )

        adsFullOptionMenu = InterstitialSingleReqAdManager(
            this,
            BuildConfig.full_menu_01,
            BuildConfig.full_menu_02
        )
    }

    private fun initApplovinMediation() {
        AppLovinSdk.getInstance(this).mediationProvider = AppLovinMediationProvider.MAX
        AppLovinSdk.getInstance(this).initializeSdk {}
    }

    fun isLockScreenInBackStack(): Boolean {
        return isLockScreenRunning
    }

    fun refreshBitmapBackground() {
        bmImg = if (AppUtils.getThemePath(this).isNotEmpty()) {
            BitmapFactory.decodeFile(AppUtils.getThemePath(this))
        } else {
            val bit = AppUtils.drawableToBitmap(this, R.drawable.theme_default)
            bit.let { AppUtils.blurStack(it, 100, false) }
        }
    }

    private fun addDefaultEqualizer(context: Context, eqDao: EqualizerDaoDB) { /*size =11*/
        eqDao.insertPreset(
            CustomPresetItem(
                "Normal", 300, 0, 0, 0, 300
            )
        )
        eqDao.insertPreset(
            CustomPresetItem(
                "Classical", 500, 300, -200, 400, 400
            )
        )
        eqDao.insertPreset(
            CustomPresetItem(
                "Dance", 600, 0, 200, -200, 100
            )
        )
        eqDao.insertPreset(
            CustomPresetItem(
                "Flat", 0, 0, 0, 0, 0
            )
        )
        eqDao.insertPreset(
            CustomPresetItem(
                "Folk", 200, 0, 0, 200, -100
            )
        )
        eqDao.insertPreset(
            CustomPresetItem(
                "Heavy metal", 400, 100, 900, 300, 0
            )
        )
        eqDao.insertPreset(
            CustomPresetItem(
                "HipHop", 500, 300, 0, 100, 300
            )
        )
        eqDao.insertPreset(
            CustomPresetItem(
                "Jazz", 400, 200, -200, 200, 500
            )
        )
        eqDao.insertPreset(
            CustomPresetItem(
                "Pop", -100, 200, 500, 100, -200
            )
        )
        eqDao.insertPreset(
            CustomPresetItem(
                "Rock", 500, 300, -100, 300, 500
            )
        )
        eqDao.insertPreset(
            CustomPresetItem(
                EqualizerHelperDB.DEFAULT_CUSTOM, 0, 0, 0, 0, 0
            )
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppResume() {
        if (currentActivity !is PlashScreenActivity
            && currentActivity !is AdActivity
        ) {
            currentActivity?.let {
                val dialogLoadingAds = DialogLoadingOpenAds(currentActivity!!)
                dialogLoadingAds.showDialogLoading()
                appOpenAdManager?.loadAd(onAdLoader = {
                    dialogLoadingAds.dismissDialog()
                    appOpenAdManager?.showAdIfAvailable(it)
                }, onAdLoadFail = {
                    dialogLoadingAds.dismissDialog()
                })
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
    }
}