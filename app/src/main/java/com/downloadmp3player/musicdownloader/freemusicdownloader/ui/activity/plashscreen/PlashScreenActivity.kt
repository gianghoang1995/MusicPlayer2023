package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.plashscreen

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.list.ListSongActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.main.MainActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.activity.permission.PermissionActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.ConfigApp
import com.aliendroid.alienads.AliendroidInitialize
import com.aliendroid.alienads.MaxIntertitial
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.BuildConfig
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseActivity
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.ActivityPlashBinding
import com.google.firebase.analytics.FirebaseAnalytics
import org.json.JSONObject

class PlashScreenActivity : BaseActivity<ActivityPlashBinding>() {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun bindingProvider(inflater: LayoutInflater): ActivityPlashBinding {
        return ActivityPlashBinding.inflate(inflater)
    }

    override fun ActivityPlashBinding.initView() {
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this@PlashScreenActivity)
        AliendroidInitialize.SelectAdsApplovinMax(this@PlashScreenActivity, "", "")
        MaxIntertitial.LoadIntertitialApplovinMax(
            this@PlashScreenActivity,
            getString(R.string.appvolin_full)
        )
        requestCheckUpdate()
    }

    private fun requestCheckUpdate() {
        if (AppUtils.isOnline(this)) {
            AndroidNetworking.get(BuildConfig.URL_CONFIG_APP)
                .setTag(this)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        val data = response.toString()
                        ConfigApp.getInstance(this@PlashScreenActivity)?.putConfigApp(data)
                        checkFinishPermission()
                    }

                    override fun onError(anError: ANError?) {
                        checkFinishPermission()
                    }
                })
        } else {
            checkFinishPermission()
        }
    }

    fun checkFinishPermission() {
        if (AppUtils.isGrantPermission(this)) {
            val folderPath = intent.getStringExtra(AppConstants.SHORTCUT_FOLDER_PATH)
            val artistID = intent.getStringExtra(AppConstants.SHORTCUT_ARTIST_ID)
            val artistName = intent.getStringExtra(AppConstants.SHORTCUT_ARTIST_NAME)
            val albumID = intent.getStringExtra(AppConstants.SHORTCUT_ALBUM_ID)
            val albumName = intent.getStringExtra(AppConstants.SHORTCUT_ALBUM_NAME)
            val intent: Intent = if (!TextUtils.isEmpty(folderPath)) {
                Intent(
                    this@PlashScreenActivity,
                    MainActivity::class.java
                ).putExtra(AppConstants.SHORTCUT_FOLDER_PATH, folderPath)
            } else if (!TextUtils.isEmpty(artistID)) {
                Intent(
                    this@PlashScreenActivity,
                    ListSongActivity::class.java
                ).putExtra(AppConstants.SHORTCUT_ARTIST_ID, artistID)
                    .putExtra(AppConstants.SHORTCUT_ARTIST_NAME, artistName)
            } else if (!TextUtils.isEmpty(albumID)) {
                Intent(
                    this@PlashScreenActivity,
                    ListSongActivity::class.java
                ).putExtra(AppConstants.SHORTCUT_ALBUM_ID, albumID)
                    .putExtra(AppConstants.SHORTCUT_ALBUM_NAME, albumName)
            } else {
                Intent(this@PlashScreenActivity, MainActivity::class.java)
                    .putExtra(AppConstants.SHOW_ADS, true)
            }
            if (AppUtils.isOnline(this)) {
                BaseApplication.mInstance?.adsFullPlash?.loadAds(onAdLoader = {
                    startActivity(intent)
                    finish()
                }, onAdLoadFail = {
                    startActivity(intent)
                    finish()
                })
            } else {
                startActivity(intent)
                finish()
            }
        } else {
            BaseApplication.mInstance?.adsFullPlash?.loadAds(onAdLoader = {
                startActivity(Intent(this@PlashScreenActivity, PermissionActivity::class.java))
                finish()
            }, onAdLoadFail = {
                startActivity(Intent(this@PlashScreenActivity, PermissionActivity::class.java))
                finish()
            })
        }
    }
}
