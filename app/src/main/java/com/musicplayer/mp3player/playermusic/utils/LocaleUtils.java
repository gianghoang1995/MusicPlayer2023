package com.musicplayer.mp3player.playermusic.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;

import java.util.Locale;

public class LocaleUtils {


    public static String PREF_SETTING_LANGUAGE = "PREF_SETTING_LANGUAGE";

    public static void applyLocale(Context context) {
        String codeLanguageCurrent = PreferenceUtils.INSTANCE.getValueString(PREF_SETTING_LANGUAGE);

        if (TextUtils.isEmpty(codeLanguageCurrent)) {
            codeLanguageCurrent = Locale.getDefault().getLanguage();
        }

        Locale newLocale = new Locale(codeLanguageCurrent);
        updateResource(context, newLocale);
    }

    public static void updateResource(Context context, Locale locale) {
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Locale current = getLocaleCompat(resources);
        if (current == locale) {
            return;
        }
        Configuration configuration = new Configuration(resources.getConfiguration());
        if (isAtLeastSdkVersion(Build.VERSION_CODES.N)) {
            configuration.setLocale(locale);
        } else if (isAtLeastSdkVersion(Build.VERSION_CODES.JELLY_BEAN_MR1)) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    public static Locale getLocaleCompat(Resources resources) {
        Configuration configuration = resources.getConfiguration();
        return isAtLeastSdkVersion(Build.VERSION_CODES.N) ? configuration.getLocales().get(0) : configuration.locale;
    }

    public static void applyLocaleAndRestart(Activity currentActivity, Class newActivity, String localeString) {
        PreferenceUtils.INSTANCE.put(PREF_SETTING_LANGUAGE, localeString);
        LocaleUtils.applyLocale(currentActivity);
        Intent intent = new Intent(currentActivity, newActivity);
        currentActivity.startActivity(intent);
        currentActivity.finish();
    }

    public static boolean isAtLeastSdkVersion(int versionCode) {
        return Build.VERSION.SDK_INT >= versionCode;
    }

}