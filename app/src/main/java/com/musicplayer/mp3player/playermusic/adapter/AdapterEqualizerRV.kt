package com.musicplayer.mp3player.playermusic.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.adapter.viewholder.EqualizerViewHolder
import com.musicplayer.mp3player.playermusic.model.CustomPresetItem

class AdapterEqualizerRV(
    private val context: Context?,
    private val equalizerOnClick: (Int) -> Unit,
    private val equalizerDelete: (Int) -> Unit
) :
    RecyclerView.Adapter<EqualizerViewHolder>() {
    var listEqualizer = ArrayList<CustomPresetItem>()
    var index: Int = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EqualizerViewHolder {
        var layoutInflater = LayoutInflater.from(context)
        return EqualizerViewHolder(context, layoutInflater, parent)
    }

    override fun getItemCount(): Int {
        return listEqualizer.size
    }

    override fun onBindViewHolder(holder: EqualizerViewHolder, position: Int) {
        holder.bind(listEqualizer[position])

        if (index == position) {
            holder.tvTitle.setTextColor(Color.parseColor("#FFB300"))
            holder.btnDelete.setImageResource(R.drawable.ic_delete_on)
            holder.itemView.setBackgroundResource(R.drawable.bg_border_equalizer_on)
        } else {
            holder.tvTitle.setTextColor(Color.parseColor("#9A9A9A"))
            holder.btnDelete.setImageResource(R.drawable.ic_delete_off)
            holder.itemView.setBackgroundResource(R.drawable.bg_border_equalizer_off)
        }

        holder.itemView.setOnClickListener {
            setIndexValue(position)
            equalizerOnClick(position)
        }

        holder.btnDelete.setOnClickListener {
            if(listEqualizer.isNotEmpty()) {
                equalizerDelete(position)
                listEqualizer.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, listEqualizer.size)
            }
        }
    }

    fun setIndexValue(value: Int) {
        index = value
        notifyDataSetChanged()
    }

    fun setDataEqualizer(list: ArrayList<CustomPresetItem>) {
        listEqualizer.clear()
        listEqualizer.addAll(list)
        notifyDataSetChanged()
    }
}