package com.musicplayer.mp3player.playermusic.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.playermusic.adapter.viewholder.SortPlaylistViewHolder
import com.musicplayer.mp3player.playermusic.model.PlaylistITem
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

class SortPlaylistAdapterRV(private val context: Context?) : RecyclerView.Adapter<SortPlaylistViewHolder>(), FastScrollRecyclerView.SectionedAdapter {
    var listPlaylist = ArrayList<PlaylistITem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortPlaylistViewHolder {
        var layoutInflater = LayoutInflater.from(context)
        return SortPlaylistViewHolder(context, layoutInflater, parent)
    }

    override fun getItemCount(): Int {
        return listPlaylist.size
    }

    @SuppressLint("SetTextI18n") override fun onBindViewHolder(holder: SortPlaylistViewHolder, position: Int) {
        val item = listPlaylist[position]
        holder.bind(item)
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