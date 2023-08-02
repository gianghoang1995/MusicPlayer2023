package com.utils.adsloader

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.utils.adsloader.utils.AdsConfigUtils
import com.utils.adsloader.utils.Constants
import com.utils.adsloader.utils.Utils
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialPreloadAdManager constructor(
    private val context: Context,
    private val mIdAdsFull01: String,
    private val mIdAdsFull02: String
) {
    companion object {
        var isShowingAds = false
    }

    private var mInterstitialAd: InterstitialAd? = null

    fun loadAds(
        onAdLoader: (() -> Unit)? = null, onAdLoadFail: (() -> Unit)? = null
    ) {
        if (!Utils.isOnline(context)) {
            onAdLoadFail?.invoke()
            return
        }
        val requestConfiguration =
            RequestConfiguration.Builder().setTestDeviceIds(Constants.testDevices()).build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        requestAdsPrepare(mIdAdsFull01, onAdLoader, onAdLoadFail = {
            requestAdsPrepare(mIdAdsFull02, onAdLoader, onAdLoadFail = {
                onAdLoadFail?.invoke()
            })
        })
    }

    private fun requestAdsPrepare(
        idAds: String, onAdLoader: (() -> Unit)? = null, onAdLoadFail: (() -> Unit)? = null
    ) {
        val requestConfiguration = RequestConfiguration.Builder().build()
        MobileAds.setRequestConfiguration(requestConfiguration)

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, idAds, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
                onAdLoadFail?.invoke()
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
                onAdLoader?.invoke()
            }
        })
    }

    fun showAds(activity: Activity, callBack: InterstitialAdListener?) {
        isShowingAds = true
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    callBack?.onClose()
                    isShowingAds = false
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    callBack?.onError()
                    isShowingAds = false
                }

                override fun onAdShowedFullScreenContent() {
                    mInterstitialAd = null
                    isShowingAds = false
                }

            }
            mInterstitialAd?.setOnPaidEventListener {
                Utils.postRevenueAdjust(context, it, mInterstitialAd?.adUnitId)
            }
            mInterstitialAd?.show(activity) ?: callBack?.onError()
        } else {
            callBack?.onError()
        }
    }

    interface InterstitialAdListener {
        fun onClose()
        fun onError()
    }


}