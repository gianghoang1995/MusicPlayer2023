package com.musicplayer.mp3player.defaultmusicplayer.ui.activity.language

import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.BuildConfig
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseActivity
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseApplication
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.databinding.ActivitySelectLanguageBinding
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.permission.PermissionActivity
import com.musicplayer.mp3player.defaultmusicplayer.utils.AppConstants
import com.musicplayer.mp3player.defaultmusicplayer.utils.LanguageUtils
import com.musicplayer.mp3player.defaultmusicplayer.utils.LocaleUtils
import com.musicplayer.mp3player.defaultmusicplayer.utils.PreferenceUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.utils.adsloader.InterstitialPreloadAdManager
import com.utils.adsloader.NativeAdsManager

class SelectLanguageActivity : BaseActivity<ActivitySelectLanguageBinding>() {

    var listObject = ArrayList<Any>()
    private var languageAdapter: LanguageAdapter? = null

    override fun onStart() {
        super.onStart()
        if (intent.getBooleanExtra(AppConstants.SHOW_ADS, false)) {
            BaseApplication.isShowAdsFull = true
            BaseApplication.mInstance?.adsFullPlash?.showAds(this,
                object : InterstitialPreloadAdManager.InterstitialAdListener {
                    override fun onClose() {

                    }

                    override fun onError() {
                    }
                })
        }
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivitySelectLanguageBinding {
        return ActivitySelectLanguageBinding.inflate(layoutInflater)
    }

    override fun ActivitySelectLanguageBinding.initView() {
        initThemeStyle(binding.supportThemes.imgTheme, binding.supportThemes.blackTspView)
        init()
    }

    fun init() {

        listObject.addAll(LanguageUtils.listLanguageSupport(this@SelectLanguageActivity))
        languageAdapter = LanguageAdapter(
            this@SelectLanguageActivity,
            listObject,
            itemClick = {
            })

        loadNativeItem()

        binding.rcvLanguage.apply {
            layoutManager = GridLayoutManager(this@SelectLanguageActivity, 1)
            adapter = languageAdapter
        }

        binding.imvSave.setOnClickListener {
            FirebaseAnalytics.getInstance(this).logEvent("click_save_language", null)
            PreferenceUtils.putFirstSelectLanguage(this@SelectLanguageActivity, false)
            LocaleUtils.applyLocaleAndRestart(
                this,
                PermissionActivity::class.java,
                languageAdapter?.getKeyLanguageSelected()
            )
        }

        binding.btnUseCurrentDeviceLang.setOnClickListener {
            FirebaseAnalytics.getInstance(this).logEvent("click_save_language", null)
            PreferenceUtils.putFirstSelectLanguage(this@SelectLanguageActivity, false)
            LocaleUtils.applyLocaleAndRestart(
                this,
                PermissionActivity::class.java,
                BaseApplication.getAppInstance().currentKeyLang
            )
        }
    }

    private fun loadNativeItem() {
        var nativeAdsLanguage = NativeAdsManager(
            this,
            BuildConfig.native_ads_language_01,
            BuildConfig.native_ads_language_02
        )

        binding.nativeAdMediumView.showShimmer(true)
        nativeAdsLanguage.loadAds(onLoadSuccess = {
            binding.nativeAdMediumView.showShimmer(false)
            binding.nativeAdMediumView.setNativeAd(it)
            binding.nativeAdMediumView.isVisible = true
            binding.imvSave.isVisible = true
            binding.btnUseCurrentDeviceLang.isVisible = true
            binding.waitLoadAds.isVisible = false
        }, onLoadFail = {
            binding.nativeAdMediumView.errorShimmer()
            binding.nativeAdMediumView.visibility = View.GONE
            binding.imvSave.isVisible = true
            binding.btnUseCurrentDeviceLang.isVisible = true
            binding.waitLoadAds.isVisible = false
        })
        /*
                var adaptiveBannerManager = AdaptiveBannerManager(
                    this,
                    BuildConfig.banner_language_id1,
                    BuildConfig.banner_language_id2,

                    )
                adaptiveBannerManager?.loadBanner(binding?.bannerAds, onAdLoader = {
                    binding?.imvSave?.isVisible = true
                    binding?.waitLoadAds?.isVisible = false
                }, onAdLoadFail = {
                    binding?.imvSave?.isVisible = true
                    binding?.waitLoadAds?.isVisible = false
                })*/
    }

    override fun onBackPressed() {

    }
}