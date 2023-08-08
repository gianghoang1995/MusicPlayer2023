package com.musicplayer.mp3player.playermusic.adapter.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.imageview.ShapeableImageView
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.model.ItemMusicOnline
import com.musicplayer.mp3player.playermusic.utils.AppUtils

class OnlineNowPlayingViewHolder(
    private val context: Context?,
    inflater: LayoutInflater,
    parent: ViewGroup
) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_now_playing_online, parent, false)) {
    var btnDrag: ImageView = itemView.findViewById(R.id.btnDrag)
    var tvTitle: TextView = itemView.findViewById(R.id.tv_album_name)
    var tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
    var btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    var imgThumb: ShapeableImageView = itemView.findViewById(R.id.imgThumb)
    var lottieNextSong: LottieAnimationView = itemView.findViewById(R.id.lottieNextSong)

    @SuppressLint("SetTextI18n")
    fun bind(item: ItemMusicOnline) {
        tvTitle.text = item.title
        tvDuration.text = AppUtils.convertDuration(item.duration)
        if (context != null) {
            Glide.with(context)
                .load(item.resourceThumb)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_song_transparent)
                .into(imgThumb)
        }
    }
}