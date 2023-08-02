package com.musicplayer.mp3player.defaultmusicplayer.utils

import android.content.Context
import android.graphics.Point
import android.util.TypedValue
import android.view.WindowManager

object Pixels {
    private var screenWidth:Int = 0
    fun getScreenWidth(c: Context): Int {
        if (screenWidth == 0) {
            val wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val size = Point()
            display.getSize(size)
            screenWidth = size.x
        }
        return screenWidth
    }


    fun pxtodp(context: Context, value: Int): Int {
        val r = context.resources
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value.toFloat(),
                r.displayMetrics
        ).toInt()
    }

}