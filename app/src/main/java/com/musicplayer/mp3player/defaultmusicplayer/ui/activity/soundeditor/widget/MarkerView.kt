package com.musicplayer.mp3player.defaultmusicplayer.ui.activity.soundeditor.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.RelativeLayout
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R
import kotlin.math.sqrt

/**
 * Represents a draggable start or end marker.
 *
 *
 * Most events are passed back to the client class using a
 * listener interface.
 *
 *
 * This class directly keeps track of its own velocity, though,
 * accelerating as the user holds down the left or right arrows
 * while this control is focused.
 *
 *
 * Modified by Anna Stępień <anna.stepien></anna.stepien>@semantive.com>
 */
class MarkerView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
    internal var TAG = "MarkerView"

    private var mVelocity: Int = 0
    private var mListener: MarkerListener? = null

    interface MarkerListener {
        fun markerTouchStart(marker: MarkerView, pos: Float)

        fun markerTouchMove(marker: MarkerView, pos: Float)

        fun markerTouchEnd(marker: MarkerView)

        fun markerFocus(marker: MarkerView)

        fun markerLeft(marker: MarkerView, velocity: Int)

        fun markerRight(marker: MarkerView, velocity: Int)

        fun markerEnter(marker: MarkerView)

        fun markerKeyUp()

        fun markerDraw()
    }

    init {


        val a = getContext().obtainStyledAttributes(
                attrs,
                R.styleable.MarkerView)
        val rotation = a.getInt(R.styleable.MarkerView_setRotation, 0)
        val r = this.resources
        val mMarkerImage = ImageView(context)
        if (rotation == 180) {
            mMarkerImage.setImageResource(R.drawable.marker_right)

        } else {
            mMarkerImage.setImageResource(R.drawable.marker_left)
        }


        val mMarkerParent = RelativeLayout.LayoutParams(pxtodp(r, 30), pxtodp(r, 30))
        mMarkerImage.layoutParams = mMarkerParent
        mMarkerParent.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

        addView(mMarkerImage)
        // Make sure we get keys
        isFocusable = true

        mVelocity = 0
        mListener = null
        a.recycle()
    }


    private fun pxtodp(r: Resources, value: Int): Int {

        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value.toFloat(),
                r.displayMetrics
        ).toInt()
    }

    fun setListener(listener: MarkerListener) {
        mListener = listener
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                requestFocus()
                // We use raw x because this window itself is going to
                // move, which will screw up the "local" coordinates
                mListener!!.markerTouchStart(this, event.rawX)
            }
            MotionEvent.ACTION_MOVE -> mListener!!.markerTouchMove(this, event.rawX)
            MotionEvent.ACTION_UP -> mListener!!.markerTouchEnd(this)
        }
        return true
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        if (gainFocus && mListener != null)
            mListener!!.markerFocus(this)
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        if (mListener != null)
            mListener!!.markerDraw()

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        mVelocity++
        val v = sqrt((1 + mVelocity / 2).toDouble()).toInt()
        if (mListener != null) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                mListener!!.markerLeft(this, v)
                return true
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                mListener!!.markerRight(this, v)
                return true
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                mListener!!.markerEnter(this)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        mVelocity = 0
        if (mListener != null)
            mListener!!.markerKeyUp()
        return super.onKeyDown(keyCode, event)
    }
}
