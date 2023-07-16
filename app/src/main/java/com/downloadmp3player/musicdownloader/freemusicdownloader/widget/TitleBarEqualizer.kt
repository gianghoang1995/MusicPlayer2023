package com.downloadmp3player.musicdownloader.freemusicdownloader.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.downloadmp3player.musicdownloader.freemusicdownloader.R


class TitleBarEqualizer : View {

    lateinit var listTitle: Array<CharSequence>
    var textPaint: Paint = Paint()
    var mWidth: Int = 0
    var mHeight: Int = 0
    var textSize = 12 * resources.displayMetrics.density
    var isTop = false

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
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.TitleBarEqualizer, 0, 0)
        listTitle =
            typedArray.getTextArray(R.styleable.TitleBarEqualizer_listTitle)

        isTop =
            typedArray.getBoolean(R.styleable.TitleBarEqualizer_isTop, false)

        textPaint.textSize = textSize.toFloat()
        textPaint.isAntiAlias = true
        textPaint.setColor(Color.parseColor("#ffffff"))
        textPaint.style = Paint.Style.FILL
        textPaint.textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        var oldWidth = 0
        mWidth = width
        mHeight = height
        val customTypeface = ResourcesCompat.getFont(context, R.font.baloo_medium)
        textPaint.typeface = customTypeface
        if (isTop)
            for (item in listTitle) {
                canvas?.drawText(
                    item.toString(),
                    oldWidth.toFloat() + (mWidth / listTitle.size) / 2,
                    mHeight.toFloat(),
                    textPaint
                )
                oldWidth += mWidth / listTitle.size
            }
        else
            for (item in listTitle) {
                canvas?.drawText(
                    item.toString(),
                    oldWidth.toFloat() + (mWidth / listTitle.size) / 2,
                    textSize,
                    textPaint
                )
                oldWidth += mWidth / listTitle.size
            }
    }

    fun setText(values: String, index: Int) {
        listTitle[index] = values
        invalidate()
    }
}