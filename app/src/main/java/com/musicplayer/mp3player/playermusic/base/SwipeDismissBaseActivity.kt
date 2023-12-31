package com.musicplayer.mp3player.playermusic.base

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

abstract class SwipeDismissBaseActivity : AppCompatActivity() {
    private var gestureDetector: GestureDetector? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gestureDetector = GestureDetector(SwipeDetector())
    }

    private inner class SwipeDetector : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            // Check movement along the Y-axis. If it exceeds SWIPE_MAX_OFF_PATH,
            // then dismiss the swipe.
            if (abs(e1.y - e2.y) > SWIPE_MAX_OFF_PATH) return false
            // Swipe from left to right.
            // The swipe needs to exceed a certain distance (SWIPE_MIN_DISTANCE)
            // and a certain velocity (SWIPE_THRESHOLD_VELOCITY).
            if (e2.x - e1.x > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                finish()
                return true
            }
            return false
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // TouchEvent dispatcher.
        if (gestureDetector != null) {
            if (ev?.let { gestureDetector!!.onTouchEvent(it) } == true) // If the gestureDetector handles the event, a swipe has been
            // executed and no more needs to be done.
                return true
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return event?.let { gestureDetector?.onTouchEvent(it) } == true
    }

    companion object {
        private const val SWIPE_MIN_DISTANCE = 120
        private const val SWIPE_MAX_OFF_PATH = 250
        private const val SWIPE_THRESHOLD_VELOCITY = 200
    }
}