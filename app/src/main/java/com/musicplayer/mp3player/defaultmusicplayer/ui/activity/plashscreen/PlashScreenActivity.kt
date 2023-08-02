package com.musicplayer.mp3player.defaultmusicplayer.ui.activity.plashscreen

import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseApplication
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.list.ListSongActivity
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.main.MainActivity
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.permission.PermissionActivity
import com.musicplayer.mp3player.defaultmusicplayer.utils.AppConstants
import com.musicplayer.mp3player.defaultmusicplayer.utils.AppUtils
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseActivity
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.databinding.ActivityPlashBinding
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.language.SelectLanguageActivity
import com.musicplayer.mp3player.defaultmusicplayer.utils.PreferenceUtils
import com.google.firebase.analytics.FirebaseAnalytics

class PlashScreenActivity : BaseActivity<ActivityPlashBinding>() {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun bindingProvider(inflater: LayoutInflater): ActivityPlashBinding {
        return ActivityPlashBinding.inflate(inflater)
    }

    override fun ActivityPlashBinding.initView() {
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this@PlashScreenActivity)
        checkFinishPermission()
    }

    private fun checkFinishPermission() {
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
                if (PreferenceUtils.isFirstSelectLanguage()) {
                    val intent =
                        Intent(this@PlashScreenActivity, SelectLanguageActivity::class.java)
                            .putExtra(AppConstants.SHOW_ADS, true)
                    startActivity(intent)
                } else {
                    startActivity(Intent(this@PlashScreenActivity, PermissionActivity::class.java))
                    finish()
                }
            }, onAdLoadFail = {
                if (PreferenceUtils.isFirstSelectLanguage()) {
                    val intent =
                        Intent(this@PlashScreenActivity, SelectLanguageActivity::class.java)
                            .putExtra(AppConstants.SHOW_ADS, true)
                    startActivity(intent)
                } else {
                    startActivity(Intent(this@PlashScreenActivity, PermissionActivity::class.java))
                    finish()
                }
            })
        }
    }
}
