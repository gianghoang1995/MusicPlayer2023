package com.musicplayer.mp3player.defaultmusicplayer.adapter.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.imageview.ShapeableImageView
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R
import com.musicplayer.mp3player.defaultmusicplayer.database.thumnail.AppDatabase
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem
import com.musicplayer.mp3player.defaultmusicplayer.utils.AppUtils
import com.musicplayer.mp3player.defaultmusicplayer.utils.ArtworkUtils

class SongVHolder(private val context: Context?, inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_song, parent, false)) {
    var imgThumb: ShapeableImageView = itemView.findViewById(R.id.imgThumb)
    var tvTitle: TextView = itemView.findViewById(R.id.tv_album_name)
    var tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
    var btnMore: ImageButton = itemView.findViewById(R.id.btn_more_album)
    var btnDrag: ImageView = itemView.findViewById(R.id.btnDrag)
    val thumbStoreDB = context?.let { AppDatabase(it) }

    @SuppressLint("SetTextI18n")
    fun bind(song: MusicItem) {
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
    }
}