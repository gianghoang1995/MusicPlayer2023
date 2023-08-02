package com.musicplayer.mp3player.defaultmusicplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.defaultmusicplayer.adapter.listener.OnClickFolderListener
import com.musicplayer.mp3player.defaultmusicplayer.adapter.viewholder.FolderVHolder
import com.musicplayer.mp3player.defaultmusicplayer.adapter.viewholder.SongVHolder
import com.musicplayer.mp3player.defaultmusicplayer.model.FolderItem
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem

class FolderAdapter(private val context: Context?, private val listener: OnClickFolderListener?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listFolder = ArrayList<FolderItem>()
    var listSong = ArrayList<MusicItem>()

    override fun onCreateViewHolder(viewgroup: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(viewgroup?.context)
        return when (p1) {
            ViewType.TYPE_FOLDER.type -> {
                FolderVHolder(context, inflater, viewgroup)
            }
            else -> {
                SongVHolder(context, inflater, viewgroup)
            }
        }
    }

    override fun getItemCount(): Int {
        return listFolder.count() + listSong.count()
    }

    override fun onBindViewHolder(viewholder: RecyclerView.ViewHolder, p1: Int) {
        viewholder.apply {
            when (viewholder) {
                is FolderVHolder -> {
                    viewholder.bind(listFolder[p1])
                    viewholder.itemView.setOnClickListener {
                        listener?.onFolderClick(listFolder[p1], p1)
                    }
                }
                is SongVHolder -> {
                    listSong[p1].let { viewholder.bind(it) }
                    viewholder.btnMore.visibility = View.GONE
                    viewholder.itemView.setOnClickListener {
                        if (listener != null)
                            listSong[p1].let { it1 ->
                                listener.onSongClick(
                                    it1,
                                    p1
                                )
                            }
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (listFolder.size > 0) {
            return ViewType.TYPE_FOLDER.type
        } else {
            return ViewType.TYPE_SONG.type
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(listF: ArrayList<FolderItem>?, listS: ArrayList<MusicItem>?) {
        listSong.clear()
        listFolder.clear()
        if (listF != null) {
            listFolder.addAll(listF)
        }

        if (listS != null) {
            listSong.addAll(listS)
        }
        notifyDataSetChanged()
    }
}

enum class ViewType(val type: Int) {
    TYPE_FOLDER(0),
    TYPE_SONG(1)
}