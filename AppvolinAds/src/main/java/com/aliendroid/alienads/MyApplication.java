package com.aliendroid.alienads;

import android.app.Application;
import android.util.Log;

import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
public class MyApplication extends Application {
    //Uranus
    @Override
    public void onCreate() {
        super.onCreate();
        AppLovinSdk.getInstance( MyApplication.this ).setMediationProvider( AppLovinMediationProvider.MAX );
        AppLovinSdk.getInstance( MyApplication.this ).initializeSdk( config -> {
        } );
    }
}