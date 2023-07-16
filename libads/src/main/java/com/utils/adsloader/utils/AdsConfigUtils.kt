package com.utils.adsloader.utils

import android.content.Context
import android.content.SharedPreferences

class AdsConfigUtils(context: Context) {
    companion object {
        const val DEF_CONFIG_NUMBER = "AdsConfigUtils_DEF_CONFIG_NUMBER"
        const val DEF_POS = "def_position"
        const val DEF_POS_VALUE = 2
    }

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AdsConfigUtils", Context.MODE_PRIVATE)

    fun putDefConfigNumber(value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(DEF_CONFIG_NUMBER, value)
        editor.apply()
    }

    fun getDefConfigNumber(): Int = sharedPreferences.getInt(DEF_CONFIG_NUMBER, 2)

    fun removeKey(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

}