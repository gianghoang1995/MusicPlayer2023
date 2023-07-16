package com.downloadmp3player.musicdownloader.freemusicdownloader.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.ArrayList

class ConfigApp constructor(context: Context) {
    private val mPref: SharedPreferences
    private val mContext: Context

    private val configApp: String
        get() = mPref.getString(AppConstants.PREF_CONFIG_APP, getDefaultConfig(mContext))
            ?: getDefaultConfig(mContext)

    fun putConfigApp(data: String?) {
        val editor = mPref.edit()
        editor.putString(AppConstants.PREF_CONFIG_APP, data)
        editor.apply()
    }

    private fun getDefaultConfig(context: Context): String {
        val input = context.resources.openRawResource(R.raw.config_default)
        val writer: Writer = StringWriter()
        val buffer = CharArray(1024)
        var reader: Reader? = null
        try {
            reader = BufferedReader(InputStreamReader(input, "UTF-8"))
            var n: Int
            while (reader.read(buffer).also { n = it } != -1) {
                writer.write(buffer, 0, n)
            }
        } catch (e: IOException) {
        } finally {
            try {
                input.close()
            } catch (e: IOException) {
            }
        }
        return writer.toString()
    }

    val isHideRate: Boolean
        get() {
            var isHideRate = false
            val data = configApp
            if (data.isNotEmpty()) {
                try {
                    val response = JSONObject(data)
                    isHideRate = response.getBoolean("hideRate")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            return isHideRate
        }

    val isShowDownload: Boolean
        get() {
            var isShowDownload = false
            val data = configApp
            if (data.isNotEmpty()) {
                try {
                    val response = JSONObject(data)
                    isShowDownload = response.getBoolean("show_download")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            return isShowDownload
        }

    val dataType: Int
        get() {
            var type = AppConstants.TYPE_ONLINE.TYPE_SCLOUD
            val data = configApp
            if (data.isNotEmpty()) {
                try {
                    val response = JSONObject(data)
                    type = response.getInt("data_type")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            return type
        }

    val listBlock: ArrayList<String>
        get() {
            val listPub = ArrayList<String>()
            val data = configApp
            if (data.isNotEmpty()) {
                try {
                    val response = JSONObject(data)
                    val jArray = response.getJSONArray("listpub")
                    for (i in 0 until jArray.length()) {
                        listPub.add(jArray.getString(i))
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            return listPub
        }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var sInstance: ConfigApp? = null
        fun getInstance(context: Context?): ConfigApp? {
            if (context != null) {
                if (sInstance == null) {
                    sInstance = ConfigApp(context.applicationContext)
                }
            }
            return sInstance
        }
    }

    init {
        mPref = PreferenceManager.getDefaultSharedPreferences(context)
        mContext = context
    }
}