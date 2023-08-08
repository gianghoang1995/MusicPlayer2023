package com.musicplayer.mp3player.playermusic.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.playermusic.callback.PlaylistCallBack
import com.musicplayer.mp3player.playermusic.adapter.viewholder.PlaylistViewHolder
import com.musicplayer.mp3player.playermusic.database.playlist.PlaylistSongDaoDB
import com.musicplayer.mp3player.playermusic.database.playlist.PlaylistSongSqLiteHelperDB
import com.musicplayer.mp3player.playermusic.model.PlaylistITem
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

class PlaylistAdapterRV(private val context: Context?, val isDialog: Boolean, private val listener: PlaylistCallBack?) : RecyclerView.Adapter<PlaylistViewHolder>(), FastScrollRecyclerView.SectionedAdapter {
    var listPlaylist = ArrayList<PlaylistITem>()
    var hideDotView = false
    var songListDao: PlaylistSongDaoDB? = null
    var songListSqliteHelper: PlaylistSongSqLiteHelperDB? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        var layoutInflater = LayoutInflater.from(context)
        return PlaylistViewHolder(context, layoutInflater, parent)
    }

    override fun getItemCount(): Int {
        return listPlaylist.size
    }

    @SuppressLint("SetTextI18n") override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val item = listPlaylist[position]
        holder.bind(item)
        holder.tvTitle.setTextColor(Color.WHITE)
        songListSqliteHelper =
            PlaylistSongSqLiteHelperDB(
                context,
                item.favorite_id
            )
        songListDao =
            PlaylistSongDaoDB(
                songListSqliteHelper
            )
        holder.itemView.setOnClickListener {
            listener?.onPlaylistClick(item, position)
        }

        holder.btnMore.setOnClickListener {
            listener?.onPlaylistMoreClick(item, holder.btnMore)
        }

        if (hideDotView) {
            holder.btnMore.visibility = View.GONE
        }
    }

    fun hideDotView() {
        hideDotView = true
        notifyDataSetChanged()
    }

    fun setDataPlaylist(list: ArrayList<PlaylistITem>) {
        listPlaylist.clear()
        listPlaylist.addAll(list)
        notifyDataSetChanged()
    }

    override fun getSectionName(position: Int): String {
        return listPlaylist[position].name?.get(0).toString().toUpperCase()
    }
}