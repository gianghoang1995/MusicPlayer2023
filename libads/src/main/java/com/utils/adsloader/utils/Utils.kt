package com.utils.adsloader.utils

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import com.google.android.gms.ads.AdValue
import com.google.firebase.analytics.FirebaseAnalytics


object Utils {
    fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            netInfo != null && netInfo.isConnected
        } catch (e: NullPointerException) {
            e.printStackTrace()
            false
        }
    }

    fun postRevenueAdjust(context: Context, ad: AdValue, adUnit: String?) {
        val rev = ad.valueMicros.toDouble() / 1000000
        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.AD_PLATFORM, "Admob")
        bundle.putString(FirebaseAnalytics.Param.AD_UNIT_NAME, adUnit)
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, rev)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, bundle)
    }
}