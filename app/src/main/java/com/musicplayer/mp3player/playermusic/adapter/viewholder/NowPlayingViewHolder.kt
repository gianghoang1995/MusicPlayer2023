package com.musicplayer.mp3player.playermusic.adapter.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.imageview.ShapeableImageView
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.database.thumnail.AppDatabase
import com.musicplayer.mp3player.playermusic.model.MusicItem
import com.musicplayer.mp3player.playermusic.utils.AppUtils
import com.musicplayer.mp3player.playermusic.utils.ArtworkUtils

class NowPlayingViewHolder(
    private val context: Context?,
    inflater: LayoutInflater,
    parent: ViewGroup
) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_now_playing, parent, false)) {
    var btnDrag: ImageView = itemView.findViewById(R.id.btnDrag)
    var tvTitle: TextView = itemView.findViewById(R.id.tv_album_name)
    var tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
    var btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    var equalizer: LottieAnimationView = itemView.findViewById(R.id.equalizer)
    var tvBitrate: TextView = itemView.findViewById(R.id.tvBitrate)
    var imgThumb: ShapeableImageView = itemView.findViewById(R.id.imgThumb)
    val thumbStoreDB = context?.let { AppDatabase(it) }

    @SuppressLint("SetTextI18n")
    fun bind(song: MusicItem) {
        tvTitle.text = song.title
        var duration = ""
        var dur = song.duration
        var artist = song.artist
        dur = if (dur != null) {
            AppUtils.convertDuration(dur.toLong())
        } else {
            context?.getString(R.string.unknow)
        }
        if (artist == null) {
            artist = context?.getString(R.string.unknow)
        }
        duration = "$dur - $artist"
        tvDuration.text = duration


        context?.let {
            val thumbStore = thumbStoreDB?.thumbDao()?.findThumbnailByID(song.id)
            if (thumbStore != null) {
                Glide.with(it)
                    .load(thumbStore.thumbPath)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_song_transparent)
                    .into(imgThumb)
            } else {
                val uri = ArtworkUtils.getArtworkFromSongID(song.id)
                if (!Uri.EMPTY.equals(uri)) {
                    Glide.with(it).load(uri)
                        .placeholder(R.drawable.ic_song_transparent)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgThumb)
                } else {
                    Glide.with(it).load(ArtworkUtils.uri(song.albumId))
                        .placeholder(R.drawable.ic_song_transparent).transition(
                            DrawableTransitionOptions.withCrossFade()
                        ).into(imgThumb)
                }
            }
        }
    }
}