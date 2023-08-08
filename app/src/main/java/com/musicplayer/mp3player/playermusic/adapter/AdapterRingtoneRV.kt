package com.musicplayer.mp3player.playermusic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.playermusic.adapter.viewholder.RingtoneViewHolder
import com.musicplayer.mp3player.playermusic.model.MusicItem

class AdapterRingtoneRV(private val context: Context?, private val listener: OnRingtoneItemClickListener)
    : RecyclerView.Adapter<RingtoneViewHolder>() {
    private var listRingtone: ArrayList<MusicItem> = ArrayList()
    private var isShowMore = false
    var index = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RingtoneViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        return RingtoneViewHolder(context, layoutInflater, parent)
    }

    override fun onBindViewHolder(holder: RingtoneViewHolder, position: Int) {
        val item = listRingtone[position]
        holder.bind(item)
        if (isShowMore) {
            holder.btnMore.visibility = View.VISIBLE
        } else {
            holder.btnMore.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            listener.onItemRingtoneClick(position,item)
        }

        holder.btnMore.setOnClickListener {
            listener.onItemMoreRingtoneClick(item, position)
        }

        if (index == position) {
            holder.nowPlayingView.visibility = View.VISIBLE
        } else {
            holder.nowPlayingView.visibility = View.GONE
        }
    }

    fun setIsShowMore(bool: Boolean) {
        isShowMore = bool
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listRingtone.size
    }

    fun setIndexNowPlaying(pos: Int) {
        index = pos
        notifyDataSetChanged()
    }

    fun removeItem(song: MusicItem) {
        val index = listRingtone.indexOf(song)
        if (index != -1) {
            listRingtone.remove(song)
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, itemCount)
        } else {
            for (i in 0..listRingtone.size) {
                if (song.id == listRingtone[i].id) {
                    listRingtone.removeAt(i)
                    notifyItemRemoved(i)
                    notifyItemRangeChanged(i, itemCount)
                }
            }
        }
    }

    fun setData(list: ArrayList<MusicItem>) {
        listRingtone.clear()
        listRingtone.addAll(list)
        notifyDataSetChanged()
    }

    interface OnRingtoneItemClickListener {
        fun onItemRingtoneClick(pos:Int,song: MusicItem)
        fun onItemMoreRingtoneClick(song: MusicItem, pos: Int)
    }
}