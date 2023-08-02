package com.musicplayer.mp3player.defaultmusicplayer.utils

import android.content.res.Resources
import android.util.DisplayMetrics
import kotlin.math.roundToInt

object ViewUtils {
    fun pxToDp(px: Float): Int {
        val displayMetrics = Resources.getSystem().displayMetrics
        return Math.round(px / (displayMetrics.density / DisplayMetrics.DENSITY_DEFAULT))
    }

    fun dpToPx(dp: Float): Int {
        val displayMetrics = Resources.getSystem().displayMetrics
        return (dp * (displayMetrics.density / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }


}