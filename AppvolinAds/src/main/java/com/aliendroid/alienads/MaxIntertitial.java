package com.aliendroid.alienads;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;


//Uranus
public class MaxIntertitial {

    public static MaxInterstitialAd interstitialAd;
    public ShowAdsListener mListener;

    public static void LoadIntertitialApplovinMax(Activity activity, String idIntertitial) {
        interstitialAd = new MaxInterstitialAd(idIntertitial, activity);
        try {
            interstitialAd.loadAd();
        }catch (Exception ex){

        }
    }


    public static void ShowIntertitialApplovinMax(Activity activity, String idIntertitial, ShowAdsListener mlistener) {
        if (interstitialAd != null)
            if (interstitialAd.isReady()) {
                interstitialAd.setListener(new MaxAdListener() {
                    @Override
                    public void onAdLoaded(MaxAd ad) {
                    }

                    @Override
                    public void onAdDisplayed(MaxAd ad) {
                    }

                    @Override
                    public void onAdHidden(MaxAd ad) {
                        mlistener.showAdsDone();
                        LoadIntertitialApplovinMax(activity, idIntertitial);
                    }

                    @Override
                    public void onAdClicked(MaxAd ad) {
                    }

                    @Override
                    public void onAdLoadFailed(String adUnitId, MaxError error) {
                        LoadIntertitialApplovinMax(activity, idIntertitial);
                        mlistener.showAdsDone();
                    }

                    @Override
                    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                        LoadIntertitialApplovinMax(activity, idIntertitial);
                        mlistener.showAdsDone();
                    }
                });
                interstitialAd.showAd();
            } else {
                LoadIntertitialApplovinMax(activity, idIntertitial);
                interstitialAd.loadAd();
                mlistener.showAdsDone();
            }
    }

    public interface ShowAdsListener {
        void showAdsDone();
    }
}
