package com.musicplayer.mp3player.defaultmusicplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.defaultmusicplayer.adapter.viewholder.SortPlaylistVHolder
import com.musicplayer.mp3player.defaultmusicplayer.model.PlaylistITem
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

class SortPlaylistAdapter(private val context: Context?) : RecyclerView.Adapter<SortPlaylistVHolder>(), FastScrollRecyclerView.SectionedAdapter {
    var listPlaylist = ArrayList<PlaylistITem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortPlaylistVHolder {
        var layoutInflater = LayoutInflater.from(context)
        return SortPlaylistVHolder(context, layoutInflater, parent)
    }

    override fun getItemCount(): Int {
        return listPlaylist.size
    }

    @SuppressLint("SetTextI18n") override fun onBindViewHolder(holder: SortPlaylistVHolder, position: Int) {
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