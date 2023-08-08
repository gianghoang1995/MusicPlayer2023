package com.musicplayer.mp3player.playermusic.widget.seekbar

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat

class VerticalSeekBarWrapper : FrameLayout {


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs)
    }

    fun init(attrs: AttributeSet?) {

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (useViewRotation()) {
            onSizeChangedUseViewRotation(w, h, oldw, oldh)
        } else {
            onSizeChangedTraditionalRotation(w, h, oldw, oldh)
        }
    }

    private fun onSizeChangedTraditionalRotation(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        val seekBar: VerticalSeekBar? = childSeekBar
        if (seekBar != null) {
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            val lp = seekBar.getLayoutParams() as LayoutParams
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.height = Math.max(0, h - vPadding)
            seekBar.setLayoutParams(lp)
            seekBar.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            val seekBarMeasuredWidth: Int = seekBar.getMeasuredWidth()
            seekBar.measure(
                MeasureSpec.makeMeasureSpec(
                    Math.max(0, w - hPadding),
                    MeasureSpec.AT_MOST
                ),
                MeasureSpec.makeMeasureSpec(
                    Math.max(0, h - vPadding),
                    MeasureSpec.EXACTLY
                )
            )
            lp.gravity = Gravity.TOP or Gravity.LEFT
            lp.leftMargin = (Math.max(0, w - hPadding) - seekBarMeasuredWidth) / 2
            seekBar.setLayoutParams(lp)
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    private fun onSizeChangedUseViewRotation(w: Int, h: Int, oldw: Int, oldh: Int) {
        val seekBar: VerticalSeekBar? = childSeekBar
        if (seekBar != null) {
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            seekBar.measure(
                MeasureSpec.makeMeasureSpec(
                    Math.max(0, h - vPadding),
                    MeasureSpec.EXACTLY
                ),
                MeasureSpec.makeMeasureSpec(
                    Math.max(0, w - hPadding),
                    MeasureSpec.AT_MOST
                )
            )
        }
        applyViewRotation(w, h)
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val seekBar: VerticalSeekBar? = childSeekBar
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (seekBar != null && widthMode != MeasureSpec.EXACTLY) {
            val seekBarWidth: Int
            val seekBarHeight: Int
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            val innerContentWidthMeasureSpec =
                MeasureSpec.makeMeasureSpec(Math.max(0, widthSize - hPadding), widthMode)
            val innerContentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                Math.max(0, heightSize - vPadding),
                heightMode
            )
            if (useViewRotation()) {
                seekBar.measure(innerContentHeightMeasureSpec, innerContentWidthMeasureSpec)
                seekBarWidth = seekBar.getMeasuredHeight()
                seekBarHeight = seekBar.getMeasuredWidth()
            } else {
                seekBar.measure(innerContentWidthMeasureSpec, innerContentHeightMeasureSpec)
                seekBarWidth = seekBar.getMeasuredWidth()
                seekBarHeight = seekBar.getMeasuredHeight()
            }
            val measuredWidth =
                View.resolveSizeAndState(seekBarWidth + hPadding, widthMeasureSpec, 0)
            val measuredHeight = View.resolveSizeAndState(
                seekBarHeight + vPadding,
                heightMeasureSpec,
                0
            )
            setMeasuredDimension(measuredWidth, measuredHeight)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    /*package*/
    fun applyViewRotation() {
        applyViewRotation(width, height)
    }

    private fun applyViewRotation(w: Int, h: Int) {
        val seekBar: VerticalSeekBar? = childSeekBar
        if (seekBar != null) {
            val isLTR =
                ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR
            val rotationAngle: Int = seekBar.rotationAngle
            val seekBarMeasuredWidth: Int = seekBar.getMeasuredWidth()
            val seekBarMeasuredHeight: Int = seekBar.getMeasuredHeight()
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom
            val hOffset =
                (Math.max(0, w - hPadding) - seekBarMeasuredHeight) * 0.5f
            val lp: ViewGroup.LayoutParams = seekBar.getLayoutParams()
            lp.width = Math.max(0, h - vPadding)
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            seekBar.setLayoutParams(lp)
            seekBar.setPivotX(if (isLTR) 0F else Math.max(0, h - vPadding).toFloat())
            seekBar.setPivotY(0F)
            when (rotationAngle) {
                VerticalSeekBar.ROTATION_ANGLE_CW_90 -> {
                    seekBar.setRotation(90F)
                    if (isLTR) {
                        seekBar.setTranslationX(seekBarMeasuredHeight + hOffset)
                        seekBar.setTranslationY(0F)
                    } else {
                        seekBar.setTranslationX(-hOffset)
                        seekBar.setTranslationY(seekBarMeasuredWidth.toFloat())
                    }
                }
                VerticalSeekBar.ROTATION_ANGLE_CW_270 -> {
                    seekBar.setRotation(270F)
                    if (isLTR) {
                        seekBar.setTranslationX(hOffset)
                        seekBar.setTranslationY(seekBarMeasuredWidth.toFloat())
                    } else {
                        seekBar.setTranslationX(-(seekBarMeasuredHeight + hOffset))
                        seekBar.setTranslationY(0F)
                    }
                }
            }
        }
    }

    private val childSeekBar: VerticalSeekBar?
        private get() {
            val child = if (childCount > 0) getChildAt(0) else null
            return if (child is VerticalSeekBar) child as VerticalSeekBar? else null
        }

    private fun useViewRotation(): Boolean {
        val seekBar: VerticalSeekBar? = childSeekBar
        return if (seekBar != null) {
            seekBar.useViewRotation()
        } else {
            false
        }
    }
}