package com.musicplayer.mp3player.defaultmusicplayer.adapter.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R

class ThemesRvVHolder(
    private val context: Context?,
    inflater: LayoutInflater,
    parent: ViewGroup
) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.layout_item_themes, parent, false)) {
    var imgTheme: ImageView = itemView.findViewById(R.id.imgThemeThumb)
    var imgAddLocal: ImageView = itemView.findViewById(R.id.imgAddLocal)
    var viewSelectedTheme: View = itemView.findViewById(R.id.viewSelectedTheme)

    @SuppressLint("SetTextI18n")
    fun bind(res: Any?, pos: Int) {
        if(pos==1) {
            context?.let {
                Glide.with(it)
                    .load(res)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(imgTheme)
            }
        } else{
            context?.let {
                Glide.with(it)
                    .load(res)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgTheme)
            }
        }
        if (pos == 0) {
            imgAddLocal.setImageResource(R.drawable.ic_open_gallery)
            imgAddLocal.visibility = View.VISIBLE
        } else {
            imgAddLocal.visibility = View.GONE
        }
    }
}