package com.downloadmp3player.musicdownloader.freemusicdownloader.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import android.widget.SeekBar
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.widget.seekbar.VerticalSeekBar


class SeekBarEqualizer : RelativeLayout {

    lateinit var slider: VerticalSeekBar
    var seekChangeListener: OnSeekChange? = null

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

    private fun init(attrs: AttributeSet?) {
        View.inflate(context, R.layout.layout_seekbar_equalizer, this)
        slider = findViewById(R.id.slider)
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (seekChangeListener != null) {
                        seekChangeListener?.onChange(progress)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

    fun setThumbOffset() {
        slider.thumbOffset = 0
//        slider.setPadding(0, 0, 0, 0)
    }

    fun setEnable(enable: Boolean) {
        slider.isEnabled = enable
        if (enable) {
            setProgressBarColor(Color.parseColor("#FFB300"))
        } else {
            setProgressBarColor(Color.GRAY)
        }
    }

    fun setProgressBarColor(newColor: Int) {
        val ld =
            slider.progressDrawable as LayerDrawable
        val d1 = ld
            .findDrawableByLayerId(R.id.progressshape) as ClipDrawable
        d1.setColorFilter(newColor, PorterDuff.Mode.SRC_IN)
    }

    fun onSeekChange(mSeekChangeListener: OnSeekChange) {
        seekChangeListener = mSeekChangeListener
    }

    fun setProgress(values: Int) {
        animateProgression(slider, values)
    }

    private fun animateProgression(
        mySeekBar: SeekBar,
        progress: Int
    ) {
        val animation = ObjectAnimator.ofInt(
            mySeekBar,
            "progress",
            mySeekBar.progress,
            progress
        )
        animation.duration = 1000
        animation.interpolator = DecelerateInterpolator()
        animation.start()
        mySeekBar.clearAnimation()
    }


    interface OnSeekChange {
        fun onChange(value: Int)
    }
}



