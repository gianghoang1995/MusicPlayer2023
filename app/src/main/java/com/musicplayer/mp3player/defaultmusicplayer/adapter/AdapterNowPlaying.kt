package com.musicplayer.mp3player.defaultmusicplayer.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.defaultmusicplayer.adapter.listener.OnClickSongListener
import com.musicplayer.mp3player.defaultmusicplayer.adapter.viewholder.NowPlayingVHolder
import com.musicplayer.mp3player.defaultmusicplayer.adapter.viewholder.OnlineNowPlayingVHolder
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem
import com.musicplayer.mp3player.defaultmusicplayer.model.ItemMusicOnline
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.util.*
import kotlin.collections.ArrayList

class AdapterNowPlaying(
    private val context: Context?, private val listener: OnClickSongListener?,
    private val songIndexListener: (Int) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), FastScrollRecyclerView.SectionedAdapter {

    var listSong = ArrayList<Any?>()
    var currentSong: MusicItem? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_OFFLINE) {
            val layoutInflater = LayoutInflater.from(context)
            NowPlayingVHolder(context, layoutInflater, parent)
        } else {
            val layoutInflater = LayoutInflater.from(context)
            OnlineNowPlayingVHolder(context, layoutInflater, parent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (listSong[position] is MusicItem) {
            TYPE_OFFLINE
        } else {
            TYPE_ONLINE
        }
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NowPlayingVHolder) {
            val item = listSong[position] as MusicItem
            holder.bind(item)
            holder.itemView.setOnClickListener {
                listener?.onSongClick(item, position)
            }

            holder.btnDelete.setOnClickListener {
                listener?.onSongMoreClick(item, position, holder.btnDelete)
            }

            if (currentSong != null) {
                if (currentSong?.id == item.id) {
                    holder.tvTitle.isSelected = true
                    holder.tvDuration.setTextColor(Color.parseColor("#00C8FF"))
                    holder.tvTitle.setTextColor(Color.parseColor("#00C8FF"))
                    holder.btnDelete.visibility = View.GONE
                    holder.equalizer.visibility = View.VISIBLE
                } else {
                    holder.tvTitle.isSelected = false
                    holder.tvDuration.setTextColor(Color.parseColor("#ffffff"))
                    holder.tvTitle.setTextColor(Color.parseColor("#ffffff"))
                    holder.equalizer.visibility = View.GONE
                    holder.btnDelete.visibility = View.VISIBLE
                }
            }
        } else if (holder is OnlineNowPlayingVHolder) {
            if (position == 0) {
                holder.lottieNextSong.isVisible = true
                holder.btnDrag.isVisible = false
                holder.btnDelete.isVisible = false
            } else {
                holder.lottieNextSong.isVisible = false
                holder.btnDrag.isVisible = true
                holder.btnDelete.isVisible = true
            }

            val item = listSong[position] as ItemMusicOnline
            holder.bind(item)
            holder.itemView.setOnClickListener {
                listener?.onClickItemOnline(item)
            }

            holder.btnDelete.setOnClickListener {
                listener?.onSongMoreClick(item, position, holder.btnDelete)
            }
        }
    }

    fun setData(list: ArrayList<Any?>) {
        listSong.clear()
        listSong.addAll(list)
        notifyDataSetChanged()
    }

    fun setIndexSongPlayer(song: Any?) {
        if (song is MusicItem) {
            currentSong = song
            notifyDataSetChanged()
        }
    }

    fun setIndexSongPlayer(song: Any?, position: Int) {
        if (song is MusicItem) {
            currentSong = song
            songIndexListener(position)
            notifyDataSetChanged()
        }
    }

    fun removeItem(song: Any) {
        var index = listSong.indexOf(song)
        listSong.remove(song)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, listSong.size)
    }

    fun moveItem(lastPos: Int, moveToPos: Int) {
        val moveItem: Any? = listSong[lastPos]
        listSong.removeAt(lastPos)
        listSong.add(moveToPos, moveItem)
        notifyDataSetChanged()
    }

    override fun getSectionName(position: Int): String {
        return if (getItemViewType(position) == TYPE_OFFLINE) {
            (listSong[position] as MusicItem).title?.get(0).toString().uppercase(Locale.ROOT)
        } else {
            (listSong[position] as ItemMusicOnline).title?.get(0).toString().uppercase(Locale.ROOT)
        }
    }

    companion object {
        const val TYPE_ONLINE = 0
        const val TYPE_OFFLINE = 1
    }
}