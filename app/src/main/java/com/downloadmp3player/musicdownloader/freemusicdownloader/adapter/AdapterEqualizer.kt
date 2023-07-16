package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.viewholder.EqualizerVHolder
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.CustomPresetItem

class AdapterEqualizer(
    private val context: Context?,
    private val equalizerOnClick: (Int) -> Unit,
    private val equalizerDelete: (Int) -> Unit
) :
    RecyclerView.Adapter<EqualizerVHolder>() {
    var listEqualizer = ArrayList<CustomPresetItem>()
    var index: Int = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EqualizerVHolder {
        var layoutInflater = LayoutInflater.from(context)
        return EqualizerVHolder(context, layoutInflater, parent)
    }

    override fun getItemCount(): Int {
        return listEqualizer.size
    }

    override fun onBindViewHolder(holder: EqualizerVHolder, position: Int) {
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