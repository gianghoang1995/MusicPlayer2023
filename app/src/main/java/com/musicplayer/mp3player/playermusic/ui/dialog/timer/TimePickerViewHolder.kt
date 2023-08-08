package com.musicplayer.mp3player.playermusic.ui.dialog.timer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.playermusic.equalizer.R

class TimePickerViewHolder(
    private val context: Context?,
    inflater: LayoutInflater,
    parent: ViewGroup
) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_time_picker, parent, false)) {

    var tvTime: TextView

    init {
        tvTime = itemView.findViewById(R.id.tv_Time)

    }

    fun bind(time: String) {
        if (time.length == 1)
            tvTime.text = "0" + time
        else
            tvTime.text = time
    }
}