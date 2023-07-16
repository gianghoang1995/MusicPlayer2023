package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener.OnClickPlaylistListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.viewholder.PlaylistVHolder
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.PlaylistSongDaoDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.playlist.PlaylistSongSqLiteHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.PlaylistITem
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

class PlaylistAdapter(private val context: Context?, val isDialog: Boolean, private val listener: OnClickPlaylistListener?) : RecyclerView.Adapter<PlaylistVHolder>(), FastScrollRecyclerView.SectionedAdapter {
    var listPlaylist = ArrayList<PlaylistITem>()
    var hideDotView = false
    var songListDao: PlaylistSongDaoDB? = null
    var songListSqliteHelper: PlaylistSongSqLiteHelperDB? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistVHolder {
        var layoutInflater = LayoutInflater.from(context)
        return PlaylistVHolder(context, layoutInflater, parent)
    }

    override fun getItemCount(): Int {
        return listPlaylist.size
    }

    @SuppressLint("SetTextI18n") override fun onBindViewHolder(holder: PlaylistVHolder, position: Int) {
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