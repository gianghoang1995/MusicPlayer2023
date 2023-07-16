package com.downloadmp3player.musicdownloader.freemusicdownloader.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.base.BaseApplication
import com.downloadmp3player.musicdownloader.freemusicdownloader.databinding.TimePickerViewBinding
import com.downloadmp3player.musicdownloader.freemusicdownloader.ui.dialog.timer.TimePickerAdapter
import com.mig35.carousellayoutmanager.CarouselLayoutManager
import com.mig35.carousellayoutmanager.CarouselZoomPostLayoutListener
import com.mig35.carousellayoutmanager.CenterScrollListener

class TimePickerView : RelativeLayout {
    val binding: TimePickerViewBinding = TimePickerViewBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    lateinit var minuteLayoutManager: CarouselLayoutManager
    lateinit var hourLayoutManager: CarouselLayoutManager
    lateinit var listener: OnTimePickerListener

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    private fun init(attrs: AttributeSet?) {
        hourLayoutManager = CarouselLayoutManager(CarouselLayoutManager.VERTICAL, true)
        hourLayoutManager.maxVisibleItems = 1
        hourLayoutManager.setPostLayoutListener(CarouselZoomPostLayoutListener())

        val hourAdapter =
            TimePickerAdapter(context)


        binding.imgBackground.setImageBitmap(BaseApplication.mInstance?.bmImg)

        binding.rvHour.setHasFixedSize(true)
        binding.rvHour.layoutManager = hourLayoutManager
        binding.rvHour.addOnScrollListener(CenterScrollListener())
        binding.rvHour.adapter = hourAdapter
        hourAdapter.initListHour()

        binding.rvHour.setHasFixedSize(true)
        binding.rvHour.layoutManager = hourLayoutManager
        binding.rvHour.addOnScrollListener(CenterScrollListener())
        hourAdapter.initListHour()
        binding.rvHour.adapter = hourAdapter
        hourLayoutManager.addOnItemSelectionListener { it ->
            hourAdapter.setPos(it)
        }


        /*Minute*/
        var minuteAdapter = TimePickerAdapter(context)

        minuteLayoutManager = CarouselLayoutManager(CarouselLayoutManager.VERTICAL, true)
        minuteLayoutManager.maxVisibleItems = 1
        minuteLayoutManager.setPostLayoutListener(CarouselZoomPostLayoutListener())

        binding.rvMinute.setHasFixedSize(true)
        binding.rvMinute.layoutManager = minuteLayoutManager
        binding.rvMinute.addOnScrollListener(CenterScrollListener())
        minuteAdapter.initListMinute()
        binding.rvMinute.adapter = minuteAdapter
        minuteLayoutManager.addOnItemSelectionListener { it ->
            minuteAdapter.setPos(it)
        }

        binding.btnCancel.setOnClickListener {
            listener.onCancelPick()
        }

        binding.btnChose.setOnClickListener {
            var hour = hourLayoutManager.centerItemPosition
            var minute = minuteLayoutManager.centerItemPosition
            if (hour >= 13) {
                hour -= 13
            }
            if (minute >= 60) {
                minute -= 60
            }
            val time =
                "$hour ${context.getString(R.string.hour)}: $minute ${context.getString(R.string.minute)}"
            listener.onTimePicker(time, getTimeToLong(hour, minute, 0))
        }

        binding.tv15m.setOnClickListener {
            val minute = 15
            val second = 0
            val time =
                "$minute ${context.getString(R.string.minute)}: $second ${context.getString(R.string.second)}"
            listener.onTimePicker(time, getTimeToLong(minute, second))
        }

        binding.tv30m.setOnClickListener {
            val minute = 30
            val second = 0
            val time =
                "$minute ${context.getString(R.string.minute)}: $second ${context.getString(R.string.second)}"
            listener.onTimePicker(time, getTimeToLong(minute, second))
        }

        binding.tv45m.setOnClickListener {
            val minute = 45
            val second = 0
            val time =
                "$minute ${context.getString(R.string.minute)}: $second ${context.getString(R.string.second)}"
            listener.onTimePicker(time, getTimeToLong(minute, second))
        }

        binding.tv60m.setOnClickListener {
            val minute = 60
            val second = 0
            val time =
                "$minute ${context.getString(R.string.minute)}: $second ${context.getString(R.string.second)}"
            listener.onTimePicker(time, getTimeToLong(minute, second))
        }
    }

    fun setTimePickerListener(mListener: OnTimePickerListener) {
        listener = mListener
    }

    private fun getTimeToLong(minute: Int, second: Int): Long {
        return (minute * 60000 + second * 1000).toLong()
    }

    private fun getTimeToLong(hour: Int, minute: Int, second: Int): Long {
        return (hour * 3600000 + minute * 60000 + second * 1000).toLong()
    }

    interface OnTimePickerListener {
        fun onTimePicker(time: String, millis: Long)
        fun onCancelPick()
    }
}



