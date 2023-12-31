package com.musicplayer.mp3player.playermusic.adapter.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.model.CustomPresetItem

class EqualizerViewHolder(
    private val context: Context?,
    inflater: LayoutInflater,
    parent: ViewGroup
) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_equalizer, parent, false)) {

    var btnDelete: ImageButton
    var tvTitle: TextView

    init {
        btnDelete = itemView.findViewById(R.id.btn_delete)
        tvTitle = itemView.findViewById(R.id.tv_name_equalizer)
    }

    fun bind(equalizer: CustomPresetItem) {
        tvTitle.text = equalizer.presetName
    }
}