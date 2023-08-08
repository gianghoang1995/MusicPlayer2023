package com.musicplayer.mp3player.playermusic.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.model.FolderItem

class FastFolderViewHolder(
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