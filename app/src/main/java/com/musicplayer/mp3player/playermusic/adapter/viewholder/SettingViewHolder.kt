package com.musicplayer.mp3player.playermusic.adapter.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.model.SettingItem

class SettingViewHolder(private val context: Context?, inflater: LayoutInflater, parent: ViewGroup)
    : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_setting, parent, false)) {
    var tvSetting: TextView = itemView.findViewById(R.id.tvSetting)
    var imgThumb: ImageView = itemView.findViewById(R.id.imgThumb)

    fun bind(setting: SettingItem) {
        tvSetting.text = setting.name
        Glide.with(context!!).load(setting.resource).into(imgThumb)
    }
}