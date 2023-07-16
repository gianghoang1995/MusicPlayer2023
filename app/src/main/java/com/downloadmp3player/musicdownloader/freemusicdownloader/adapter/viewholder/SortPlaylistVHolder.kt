package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.FavoriteSqliteHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.PlaylistITem

class SortPlaylistVHolder(
    private val context: Context?,
    inflater: LayoutInflater,
    parent: ViewGroup
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_sort_favorite, parent, false)) {
    var imgThumb: ShapeableImageView = itemView.findViewById(R.id.imgThumb)
    var tvTitle: TextView = itemView.findViewById(R.id.tv_playlist_name)
    fun bind(playlist: PlaylistITem) {
        context?.let {
            playlist.thumbnail?.let { it1 ->
                Glide.with(it)
                    .load("")
                    .placeholder(it1)
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
    }
}