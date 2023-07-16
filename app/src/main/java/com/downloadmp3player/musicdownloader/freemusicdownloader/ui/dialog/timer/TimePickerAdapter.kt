package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.dialog.timer

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TimePickerAdapter(
    private val context: Context?
) :
    RecyclerView.Adapter<TimePickerViewHolder>() {
    var listTime = ArrayList<String>()
    var curentPos: Int? = null
    var lastPos: Int = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimePickerViewHolder {
        var layoutInflater = LayoutInflater.from(context)
        return TimePickerViewHolder(
            context,
            layoutInflater,
            parent
        )
    }

    override fun getItemCount(): Int {
        return listTime.size
    }

    override fun onBindViewHolder(holder: TimePickerViewHolder, position: Int) {
        holder.bind(listTime[position])
        if (position == lastPos) {
            holder.tvTime.setTextColor(Color.WHITE)
        } else {
            holder.tvTime.setTextColor(Color.parseColor("#bfbdbd"))
        }
        curentPos = position
    }

    fun setPos(pos: Int) {
        lastPos = pos
        notifyDataSetChanged()
    }

    fun initListHour() {
        for (item: Int in 0..12) {
            listTime.add(item.toString())
        }
        notifyDataSetChanged()
    }

    fun initListMinute() {
        for (item: Int in 0..59) {
            listTime.add(item.toString())
        }
        notifyDataSetChanged()
    }

    fun getCurTime(): Int {
        return curentPos!!
    }
}