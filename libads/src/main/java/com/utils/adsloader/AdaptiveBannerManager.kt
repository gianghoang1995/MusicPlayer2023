package com.utils.adsloader

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.utils.adsloader.utils.Constants
import com.utils.adsloader.utils.Utils

class AdaptiveBannerManager(
    private val context: Activity,
    private val mIdBanner01: String,
    private val mIdBanner02: String,
    private val isColapse: Boolean
) {
    var adView: AdManagerAdView? = null
    var isBannerLoaded = false

    companion object {
    }

    private val adSize: AdSize
        get() {
            val display = context.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = context.resources.displayMetrics.widthPixels.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }

    fun loadBanner(
        parent: ViewGroup?, onAdLoader: (() -> Unit)? = null, onAdLoadFail: (() -> Unit)? = null
    ) {
        if (!Utils.isOnline(context)) {
            onAdLoadFail?.invoke()
            return
        }
        val requestConfiguration =
            RequestConfiguration.Builder().setTestDeviceIds(Constants.testDevices()).build()
        MobileAds.setRequestConfiguration(requestConfiguration)

        requestBannerAdsPrepare(mIdBanner01, parent, onAdLoader, onAdLoadFail = {
            requestBannerAdsPrepare(mIdBanner02, parent, onAdLoader, onAdLoadFail = {
                onAdLoadFail?.invoke()
            })
        })
    }

    private fun requestBannerAdsPrepare(
        idBanner: String,
        parent: ViewGroup?,
        onAdLoader: (() -> Unit)? = null,
        onAdLoadFail: (() -> Unit)? = null
    ) {
        adView = AdManagerAdView(context)
        adView?.adUnitId = idBanner
        adView?.setAdSizes(adSize)
        adView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                isBannerLoaded = true
                parent?.removeAllViews()
                parent?.addView(adView)
                onAdLoader?.invoke()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                onAdLoadFail?.invoke()
            }
        }
        adView?.setOnPaidEventListener {
            Utils.postRevenueAdjust(context, it, adView?.adUnitId)
        }
        val adRequest = if (isColapse) {
            AdRequest
                .Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply {
                    putString("collapsible", "bottom")
                })
                .build()
        } else {
            AdRequest.Builder().build()
        }
        adView?.loadAd(adRequest)
    }

    fun loadAdViewToParent(parent: ViewGroup?) {
        if (adView?.parent != null) {
            (adView?.parent as ViewGroup).removeAllViews()
        }
        parent?.removeAllViews()
        parent?.addView(adView)
        parent?.isVisible = true

    }

    fun stopView() {
        adView?.pause()
    }

    fun resumeView() {
        adView?.resume()
    }
}