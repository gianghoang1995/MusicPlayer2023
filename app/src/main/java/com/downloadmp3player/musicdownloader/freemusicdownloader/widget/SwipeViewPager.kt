package com.downloadmp3player.musicdownloader.freemusicdownloader.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import com.downloadmp3player.musicdownloader.freemusicdownloader.R

class SwipeViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {
    private var mSwipeEnable: Boolean = false

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SwipeViewPager)
        try {
            mSwipeEnable = a.getBoolean(R.styleable.SwipeViewPager_swipeEnable, true)
        } finally {
            a.recycle()
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return mSwipeEnable && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mSwipeEnable && super.onInterceptTouchEvent(ev)
    }

    fun setSwipeEnable(swipeEnable: Boolean) {
        mSwipeEnable = swipeEnable
    }

}
