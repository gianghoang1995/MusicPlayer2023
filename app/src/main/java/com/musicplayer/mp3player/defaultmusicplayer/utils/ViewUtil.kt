package com.musicplayer.mp3player.defaultmusicplayer.utils

import android.view.MotionEvent
import android.view.View

object ViewUtil {

    fun SetOntouchListener(view: View) {
         view.setOnTouchListener(View.OnTouchListener { view1, motionEvent ->
             val action = motionEvent.action
             if (action == MotionEvent.ACTION_DOWN) {
                 view1.animate().scaleXBy(-0.05f).setDuration(100).start()
                 view1.animate().scaleYBy(-0.05f).setDuration(100).start()
                 return@OnTouchListener false
             } else if (action == MotionEvent.ACTION_UP) {
                 view1.animate().cancel()
                 view1.animate().scaleX(1f).setDuration(100).start()
                 view1.animate().scaleY(1f).setDuration(100).start()
                 return@OnTouchListener false
             }
             false
         })
     }





}
