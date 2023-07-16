package com.utils.adsloader

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView


abstract class MLBBaseNativeAdView(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    open fun setNativeAd(nativeAd: NativeAd) {
        val icon = nativeAd.icon
        val starRating = nativeAd.starRating
        val title = nativeAd.headline
        val callAction = nativeAd.callToAction
        val price = nativeAd.price
        val subTitle = nativeAd.body

        if (icon != null) {
            getIconView().setImageDrawable(icon.drawable)
            getIconView().visibility = View.VISIBLE
        } else {
            getIconView().visibility = View.GONE
        }

        if (starRating != null && starRating > 0f) {
            getRatingView()?.rating = starRating.toFloat()
            getRatingView()?.isVisible = true
        } else {
            getRatingView()?.isGone = true
        }

        getViewContainerRate_Price()?.isVisible =
            !((starRating == null || starRating <= 0f) && price == null)

        if (subTitle != null) {
            getSubTitleView()?.text = subTitle
            getSubTitleView()?.visibility = View.VISIBLE
        } else {
            getSubTitleView()?.visibility = View.GONE
        }

        if (title != null) {
            getTitleView().text = title
            getTitleView().isSelected = true
            getTitleView().visibility = View.VISIBLE
        } else {
            getTitleView().visibility = View.GONE
        }

        if (callAction != null) {
            getCallActionButtonView().text = callAction
            getCallActionButtonView().visibility = View.VISIBLE
        } else {
            getCallActionButtonView().visibility = View.GONE
        }


        if (price != null) {
            getPriceView()?.text = price
            getPriceView()?.visibility = View.VISIBLE
        } else {
            getPriceView()?.visibility = View.GONE
        }

        if (getMediaView() != null) {
            getAdView().mediaView = getMediaView()
        }

        getAdView().callToActionView = getCallActionButtonView()
        getAdView().headlineView = getTitleView()
        getAdView().bodyView = getSubTitleView()
        getAdView().setNativeAd(nativeAd)
    }

    fun showShimmer(isShow: Boolean) {
        if (isShow) {
            getShimerView().startShimmer()
            getShimerView().isVisible = true
            getRootAds().isVisible = false
        } else {
            getShimerView().stopShimmer()
            getShimerView().isVisible = false
            getRootAds().isVisible = true
        }
    }

    fun hideAdsAndShimmer() {
        getShimerView().stopShimmer()
        getShimerView().isVisible = false
        getRootAds().isVisible = false
    }

    fun errorShimmer() {
        getShimerView().stopShimmer()
    }


    abstract fun getTitleView(): AppCompatTextView
    abstract fun getSubTitleView(): AppCompatTextView?
    abstract fun getRatingView(): AppCompatRatingBar?
    abstract fun getIconView(): ImageView
    abstract fun getPriceView(): AppCompatTextView?
    abstract fun getCallActionButtonView(): AppCompatTextView
    abstract fun getAdView(): NativeAdView
    abstract fun getShimerView(): ShimmerFrameLayout
    abstract fun getRootAds(): LinearLayout

    open fun getMediaView(): MediaView? = null
    open fun getViewContainerRate_Price(): View? = null

}