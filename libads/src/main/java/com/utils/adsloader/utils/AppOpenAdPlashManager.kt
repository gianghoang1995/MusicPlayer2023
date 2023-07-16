package com.utils.adsloader.utils

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.*


class AppOpenAdPlashManager constructor(
    private val context: Context,
    private val idOpenAds01: String,
    private val idOpenAds02: String
) {
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false

    companion object {
        var isShowingAd = false
    }

    private var loadTime: Long = 0

    fun loadAd(onAdLoader: (() -> Unit)?, onAdLoadFail: (() -> Unit)?) {
        if (isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        if (AdsConfigUtils(context).getDefConfigNumber() == 1) {
            loadAdsPrepare(idOpenAds01, onAdLoader = { onAdLoader?.invoke() }, onAdLoadFail = {
                loadAdsPrepare(idOpenAds02, onAdLoader = { onAdLoader?.invoke() }, onAdLoadFail = {
                    onAdLoadFail?.invoke()
                })
            })
        } else {
            loadAdsPrepare(idOpenAds02, onAdLoader = { onAdLoader?.invoke() }, onAdLoadFail = {
                loadAdsPrepare(idOpenAds01, onAdLoader = { onAdLoader?.invoke() }, onAdLoadFail = {
                    onAdLoadFail?.invoke()
                })
            })
        }
    }

    private fun loadAdsPrepare(
        idAds: String, onAdLoader: (() -> Unit)? = null, onAdLoadFail: (() -> Unit)? = null
    ) {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(context,
            idAds,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    onAdLoader?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    onAdLoadFail?.invoke()
                }
            })
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    fun showAdIfAvailable(
        activity: Activity,
    ) {
        showAdIfAvailable(activity, object : OnShowAdCompleteListener {
            override fun onShowAdComplete() {
                isShowingAd = false
                // Empty because the user will go back to the activity that shows the ad.
            }
        })
    }

    private fun showAdIfAvailable(
        activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        if (isShowingAd) {
            return
        }

        if (!isAdAvailable()) {
            onShowAdCompleteListener.onShowAdComplete()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                onShowAdCompleteListener.onShowAdComplete()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                onShowAdCompleteListener.onShowAdComplete()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
            }
        }
        appOpenAd?.setOnPaidEventListener {
            Utils.postRevenueAdjust(context, it, appOpenAd?.adUnitId)
        }
        appOpenAd?.show(activity)
    }

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }
}
