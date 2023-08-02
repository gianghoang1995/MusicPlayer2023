package com.musicplayer.mp3player.defaultmusicplayer.utils;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.musicplayer.mp3player.defaultmusicplayer.equalizer.BuildConfig;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdsPlashUtils {
    private static AdsPlashUtils adsUtils;
    private InterstitialAd mInterstitialAd;

    public static AdsPlashUtils getInstance() {
        if (adsUtils == null) {
            adsUtils = new AdsPlashUtils();
        }
        return adsUtils;
    }

    public void setUpInterestialAds(Context context, OnLoadAdsPlashListener listener) {
        RequestConfiguration requestConfiguration
                = new RequestConfiguration.Builder()
                .setTestDeviceIds(AppConstants.INSTANCE.testDevices())
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);
        loadAd(context, listener);
    }

    public void loadAd(Context context, OnLoadAdsPlashListener adsPlashListener) {
        loadPrepareAds(context, BuildConfig.full_plash_01, adsPlashListener, () -> {
            loadPrepareAds(context, BuildConfig.full_plash_02, adsPlashListener, adsPlashListener::onFinishLoad);
        });
    }

    public void loadPrepareAds(Context context, String id, OnLoadAdsPlashListener adsPlashListener, OnLoadAdsError onErrorListener) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                context,
                id,
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        adsPlashListener.onFinishLoad();
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        postResultShowAds();
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        postResultShowAds();
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        postResultShowAds();
                        onErrorListener.onLoadAdError();
                    }
                });
    }

    public void showInterstitial(Activity activity) {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(activity);
        }
    }

    private void postResultShowAds() {
        mInterstitialAd = null;
    }

    public interface OnLoadAdsPlashListener {
        void onFinishLoad();
    }

    public interface OnLoadAdsError {
        void onLoadAdError();
    }
}
