package com.musicplayer.mp3player.playermusic.adapter.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.model.FolderItem

class FolderViewHolder(private val context: Context?, inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_folder, parent, false)) {

    var tvTitle: TextView
    var tv_count_folder: TextView
    var btnMore: ImageView
    var imgThumb: ImageView

    init {
        tvTitle = itemView.findViewById(R.id.tv_album_name)
        tv_count_folder = itemView.findViewById(R.id.tv_count_folder)
        btnMore = itemView.findViewById(R.id.btn_more_album)
        imgThumb = itemView.findViewById(R.id.imgThumb)

    }

    fun bind(folder: FolderItem) {
        if (folder.name.equals(context?.getString(R.string.internal))) {
            imgThumb.setImageResource(R.drawable.cpu)
        } else if (folder.name.equals(context?.getString(R.string.external))) {
            imgThumb.setImageResource(R.drawable.sd_card)
        } else {
            imgThumb.setImageResource(R.drawable.ic_folder_query)
        }

        tvTitle.text = folder.name
        tv_count_folder.text = "${folder.count} ${context?.getString(R.string.title_songs)}"

    }
}