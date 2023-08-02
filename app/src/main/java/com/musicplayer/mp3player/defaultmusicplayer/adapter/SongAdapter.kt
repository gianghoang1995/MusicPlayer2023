package com.musicplayer.mp3player.defaultmusicplayer.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SectionIndexer
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.defaultmusicplayer.adapter.listener.OnClickSongListener
import com.musicplayer.mp3player.defaultmusicplayer.adapter.viewholder.SongVHolder
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem
import java.util.*
import kotlin.collections.ArrayList

class SongAdapter(
    private val context: Context?,
    private val listener: OnClickSongListener?,
    private val songIndexListener: (Int) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    SectionIndexer {
    companion object {
        const val TYPE_NATIVE_ADS = 0
        const val TYPE_MUSIC = 1
    }

    var mSectionPositions: ArrayList<Int> = ArrayList()
    var listSong = ArrayList<MusicItem>()
    var hideMoreButton = false
    var currentSong: MusicItem? = null
    var showDrag = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        return SongVHolder(context, layoutInflater, parent)
    }

    override fun getItemViewType(position: Int): Int {
        return if (listSong[position].isNativeAds) {
            TYPE_NATIVE_ADS
        } else {
            TYPE_MUSIC
        }
    }

    override fun getItemCount(): Int {
        return listSong.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = listSong[position]
        if (holder is SongVHolder) {
            holder.bind(item)
            holder.itemView.setOnClickListener {
                listener?.onSongClick(item, position)
            }

            if (showDrag) {
                holder.btnDrag.visibility = View.VISIBLE
            } else {
                holder.btnDrag.visibility = View.GONE

            }

            holder.itemView.setOnLongClickListener {
                listener?.onSongOnLongClick()
                true
            }

            holder.btnMore.setOnClickListener {
                listener?.onSongMoreClick(item, position, holder.btnMore)
            }

            if (hideMoreButton) {
                holder.btnMore.visibility = View.GONE
            } else {
                holder.btnMore.visibility = View.VISIBLE
            }

            if (item.isSelected) {
                holder.itemView.setBackgroundColor(Color.parseColor("#80FFFFFF"))
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }

            if (currentSong != null) {
                if (currentSong?.id == item.id) {
                    holder.tvDuration.setTextColor(Color.parseColor("#00C8FF"))
                    holder.tvTitle.setTextColor(Color.parseColor("#00C8FF"))
                } else {
                    holder.tvDuration.setTextColor(Color.parseColor("#E1E1E1"))
                    holder.tvTitle.setTextColor(Color.parseColor("#ffffff"))
                }
            } else {
                holder.tvDuration.setTextColor(Color.parseColor("#E1E1E1"))
                holder.tvTitle.setTextColor(Color.parseColor("#ffffff"))
            }
        }
    }

    fun showDrag(isShow: Boolean) {
        showDrag = isShow
        notifyDataSetChanged()
    }

    fun setDataSong(list: ArrayList<MusicItem>) {
        listSong.clear()
        listSong.addAll(list)
        notifyDataSetChanged()
    }

    fun updateItem(position: Int, song: MusicItem) {
        listSong[position].title = song.title
        listSong[position].songPath = song.songPath
        notifyDataSetChanged()
    }

    fun hideMoreButton(isHide: Boolean) {
        hideMoreButton = isHide
        notifyDataSetChanged()
    }

    fun setIndexSongPlayer(song: MusicItem?, position: Int) {
        currentSong = song
        songIndexListener(position)
        notifyDataSetChanged()
    }

    fun clearIndexSong() {
        currentSong = null
        songIndexListener(-1)
        notifyDataSetChanged()
    }

    fun removeItem(song: MusicItem) {
        val index = listSong.indexOf(song)
        if (index != -1) {
            listSong.remove(song)
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, itemCount)
        } else {
            for (i in 0 until listSong.size) {
                if (song.id == listSong[i].id) {
                    listSong.removeAt(i)
                    notifyItemRemoved(i)
                    notifyItemRangeChanged(i, itemCount)
                }
            }
        }
    }

    fun removeSongPosition(i: Int) {
        listSong.removeAt(i)
        notifyItemRemoved(i)
        notifyItemRangeChanged(i, itemCount)
    }

    fun updateName(oldSong: MusicItem, newSong: MusicItem) {
        for (i in 0 until listSong.size) {
            if (listSong[i].songPath.equals(oldSong.songPath)) {
                listSong[i].title = newSong.title
                listSong[i].songPath = newSong.songPath
                notifyDataSetChanged()
                break
            }
        }
    }

    override fun getSections(): Array<String> {
        val sections: MutableList<String> = ArrayList()
        mSectionPositions = ArrayList()
        var i = 0
        val size: Int = listSong.size
        while (i < size) {
            val section: String =
                java.lang.String.valueOf(listSong[i].title?.get(0)).uppercase(Locale.getDefault())
            if (!sections.contains(section)) {
                sections.add(section)
                mSectionPositions.add(i)
            }
            i++
        }
        return sections.toTypedArray()
    }


    override fun getPositionForSection(sectionIndex: Int): Int {
        return mSectionPositions[sectionIndex]
    }

    override fun getSectionForPosition(position: Int): Int {
        return 0
    }
}