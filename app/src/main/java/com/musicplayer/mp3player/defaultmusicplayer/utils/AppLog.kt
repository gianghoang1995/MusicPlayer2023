package com.musicplayer.mp3player.defaultmusicplayer.utils

import android.util.Log
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.BuildConfig

object AppLog {
    const val LOG_TAG = "tag_app | "
    var startTime = -1L
    private val hashMap = HashMap<String, Long>()
    fun d(message: String? = "empty") {
        val stackTraceElement = Throwable().stackTrace[1]
        if (BuildConfig.DEBUG) {
            Log.d(
                stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                        " at line " + stackTraceElement.lineNumber, LOG_TAG +  (message.takeIf { it?.isNotEmpty() == true } ?: "empty")
            )
        }
    }

    fun d(message: String, error: Exception) {
        val stackTraceElement = Throwable().stackTrace[1]
        if (BuildConfig.DEBUG) {
            Log.d(
                stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                        " at line " + stackTraceElement.lineNumber, LOG_TAG + message, error
            )
        }
    }

    fun e(message: Long? = 0) {
        val stackTraceElement = Throwable().stackTrace[1]
        if (BuildConfig.DEBUG) {
            Log.e(
                stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                        " at line " + stackTraceElement.lineNumber, LOG_TAG + message.toString()
            )
        }
    }

    fun checkPoint(key: String ?= null) {
        if(key != null) {
            hashMap[key] = System.currentTimeMillis()
        } else {
            startTime = System.currentTimeMillis()
        }
    }

    fun eCheckPoint(message: String? = "empty", key: String? = null) {
        val stackTraceElement = Throwable().stackTrace[1]
        if (BuildConfig.DEBUG) {
            Log.e(
                stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                        " at line " + stackTraceElement.lineNumber, LOG_TAG + ("timeCheck = ${System.currentTimeMillis() - if(hashMap[key] != null ) hashMap[key]!! else startTime}") + (message.takeIf { it?.isNotEmpty() == true } ?: "empty")
            )
        }
        startTime = -1
        hashMap.remove(key)
    }

    fun e(message: String? = "empty") {
        val stackTraceElement = Throwable().stackTrace[1]
        if (BuildConfig.DEBUG) {
            Log.e(
                stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                        " at line " + stackTraceElement.lineNumber, LOG_TAG + (message.takeIf { it?.isNotEmpty() == true } ?: "empty")
            )
        }
    }

    fun e(error: Exception) {
        val stackTraceElement = Throwable().stackTrace[1]
        if (BuildConfig.DEBUG) {
            Log.e(
                stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                        " at line " + stackTraceElement.lineNumber, LOG_TAG + "error: ", error
            )
        }
    }

    fun e(message: String, error: Exception) {
        val stackTraceElement = Throwable().stackTrace[1]
        if (BuildConfig.DEBUG) {
            Log.e(
                stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                        " at line " + stackTraceElement.lineNumber, LOG_TAG + message, error
            )
        }
    }

    fun i(message: String) {
        val stackTraceElement = Throwable().stackTrace[1]
        if (BuildConfig.DEBUG) {
            Log.i(
                stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                        " at line " + stackTraceElement.lineNumber, LOG_TAG + message
            )
        }
    }

    fun i(message: String, error: Exception) {
        val stackTraceElement = Throwable().stackTrace[1]
        if (BuildConfig.DEBUG) {
            Log.i(
                stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                        " at line " + stackTraceElement.lineNumber, LOG_TAG + message, error
            )
        }
    }

    fun w(message: String) {
        val stackTraceElement = Throwable().stackTrace[1]
        if (BuildConfig.DEBUG) {
            Log.w(
                stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                        " at line " + stackTraceElement.lineNumber, LOG_TAG + message
            )
        }
    }

    fun w(message: String, error: Exception) {
        val stackTraceElement = Throwable().stackTrace[1]
        if (BuildConfig.DEBUG) {
            Log.w(
                stackTraceElement.fileName + " in " + stackTraceElement.methodName +
                        " at line " + stackTraceElement.lineNumber, LOG_TAG + message, error
            )
        }
    }
}