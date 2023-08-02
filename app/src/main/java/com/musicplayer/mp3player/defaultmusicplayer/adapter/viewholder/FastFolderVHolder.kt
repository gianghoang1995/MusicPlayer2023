package com.musicplayer.mp3player.defaultmusicplayer.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R
import com.musicplayer.mp3player.defaultmusicplayer.model.FolderItem

class FastFolderVHolder(
    inflater: LayoutInflater,
    parent: ViewGroup
) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_holder_tolbar_folder, parent, false)) {
    var tvFastFolder: TextView
    init {
        tvFastFolder = itemView.findViewById(R.id.tvFastFolder)
    }

    fun bind(folder: FolderItem) {
        tvFastFolder.text = folder.name
    }
}