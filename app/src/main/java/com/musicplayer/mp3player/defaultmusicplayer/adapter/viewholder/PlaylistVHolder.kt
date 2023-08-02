package com.musicplayer.mp3player.defaultmusicplayer.adapter.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.imageview.ShapeableImageView
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R
import com.musicplayer.mp3player.defaultmusicplayer.database.playlist.FavoriteSqliteHelperDB
import com.musicplayer.mp3player.defaultmusicplayer.model.PlaylistITem

class PlaylistVHolder(
    private val context: Context?,
    inflater: LayoutInflater,
    parent: ViewGroup
) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_favorite, parent, false)) {
    var imgThumb: ShapeableImageView = itemView.findViewById(R.id.imgThumb)
    var tvTitle: TextView = itemView.findViewById(R.id.tv_playlist_name)
    var tvCountSong: TextView = itemView.findViewById(R.id.tvCountSong)
    var btnMore: ImageView = itemView.findViewById(R.id.btn_more_playlist)

    @SuppressLint("SetTextI18n")
    fun bind(playlist: PlaylistITem) {
        if (context != null) {
            playlist.thumbnail?.let {
                Glide.with(context).load("")
                    .placeholder(it)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imgThumb)
            }
        }
        when {
            playlist.name.equals(FavoriteSqliteHelperDB.DEFAULT_FAVORITE) -> {
                tvTitle.text = context?.getString(R.string.favorite_song)
            }
            playlist.name.equals(FavoriteSqliteHelperDB.TABLE_RECENT_ADDED) -> {
                tvTitle.text = context?.getString(R.string.favorite_recent_added)
            }
            playlist.name.equals(FavoriteSqliteHelperDB.TABLE_LAST_PLAYING) -> {
                tvTitle.text = context?.getString(R.string.favorite_last_playing)
            }
            playlist.name.equals(FavoriteSqliteHelperDB.TABLE_MOST_PLAYING) -> {
                tvTitle.text = context?.getString(R.string.favorite_most_playing)
            }
            else -> {
                tvTitle.text = playlist.name
            }
        }
        tvCountSong.text =
            playlist.countSong.toString() + " " + context?.getString(R.string.title_songs)
    }
}