package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.FolderItem

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