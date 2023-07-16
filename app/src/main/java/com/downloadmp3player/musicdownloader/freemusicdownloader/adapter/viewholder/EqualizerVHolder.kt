package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.CustomPresetItem

class EqualizerVHolder(
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