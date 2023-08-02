package com.musicplayer.mp3player.defaultmusicplayer.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.android.exoplayer2.Player
import com.musicplayer.mp3player.defaultmusicplayer.base.BaseApplication

object PreferenceUtils {
    private val PREFS_NAME = "Downloadmp3.pref"
    val sharedPref: SharedPreferences =
        BaseApplication.getAppInstance().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun put(KEY_NAME: String, value: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(KEY_NAME, value)
        editor.apply()
    }

    fun put(KEY_NAME: String, value: Float) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putFloat(KEY_NAME, value)
        editor.apply()
    }

    fun put(KEY_NAME: String, value: Long) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putLong(KEY_NAME, value)
        editor.apply()
    }

    fun put(KEY_NAME: String, value: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(KEY_NAME, value)
        editor.apply()
    }

    fun putLoopMode(value: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(AppConstants.LOOP_MODE, value)
        editor.apply()
    }


    fun put(KEY_NAME: String, status: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(KEY_NAME, status!!)
        editor.apply()
    }

    fun putFontSize(value: Float) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putFloat(AppConstants.PREF_FONT_SIZE, value)
        editor.apply()
    }

    fun getValueString(KEY_NAME: String): String? {
        return sharedPref.getString(KEY_NAME, null)
    }

    fun getValueInt(KEY_NAME: String): Int {
        return sharedPref.getInt(KEY_NAME, 0)
    }

    fun getLoopMode(): Int {
        return sharedPref.getInt(AppConstants.LOOP_MODE, Player.REPEAT_MODE_ALL)
    }

    fun getValueFloat(KEY_NAME: String): Float {
        return sharedPref.getFloat(KEY_NAME, 0f)
    }

    fun getValueInt(KEY_NAME: String, def: Int): Int {
        return sharedPref.getInt(KEY_NAME, def)
    }

    fun getFontSize(): Float {
        return sharedPref.getFloat(AppConstants.PREF_FONT_SIZE, AppConstants.FONT.NORMAL)
    }

    fun getShakeCount(): Int {
        return sharedPref.getInt(AppConstants.PREF_SHAKE_COUNT, -1)
    }

    fun getValueLong(KEY_NAME: String): Long {
        return sharedPref.getLong(KEY_NAME, 0)
    }

    fun getValueBoolean(KEY_NAME: String): Boolean {
        return sharedPref.getBoolean(KEY_NAME, false)
    }

    fun getValueBoolean(KEY_NAME: String, def: Boolean): Boolean {
        return sharedPref.getBoolean(KEY_NAME, def)
    }

    fun clearSharedPreference() {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        //sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        editor.clear()
        editor.apply()
    }

    fun removeValue(KEY_NAME: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.remove(KEY_NAME)
        editor.apply()
    }

    fun getIDPlaylist(): Int {
        return sharedPref.getInt(AppConstants.PREF_ID_PLAYLIST, 0)
    }

    fun setIDPlaylist() {
        val id = getIDPlaylist() + 1
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(AppConstants.PREF_ID_PLAYLIST, id)
        editor.apply()
    }

    fun getThemeOverlayProgress(): Int {
        return sharedPref.getInt(AppConstants.THEME_OVERLAY_PROGRESS, 20)
    }

    fun getThemeBlurProgress(): Int {
        return sharedPref.getInt(AppConstants.THEME_BLUR_PROGRESS, 100)
    }

    fun putFirstSelectLanguage(mContext: Context, selected: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(AppConstants.KEY_LANGUAGE_SELECTED, selected)
        editor.apply()
    }

    fun isFirstSelectLanguage(): Boolean {
        return sharedPref.getBoolean(AppConstants.KEY_LANGUAGE_SELECTED, true)
    }

}
