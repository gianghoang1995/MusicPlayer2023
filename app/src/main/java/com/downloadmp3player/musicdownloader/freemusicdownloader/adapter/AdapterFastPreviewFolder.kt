package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener.OnClickFastFolderListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.viewholder.FastFolderVHolder
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.FolderItem

class AdapterFastPreviewFolder(
    private val context: Context?,
    private val listener: OnClickFastFolderListener?
) :
    RecyclerView.Adapter<FastFolderVHolder>() {
    var listFolder = ArrayList<FolderItem>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FastFolderVHolder {
        var layoutInflater = LayoutInflater.from(context)
        return FastFolderVHolder(layoutInflater, parent)
    }

    override fun getItemCount(): Int {
        return listFolder.size
    }

    override fun onBindViewHolder(holder: FastFolderVHolder, position: Int) {
        val item = listFolder.get(position)
        holder.bind(item)

        if (position == listFolder.size - 1) {
            holder.tvFastFolder.setTextColor(Color.parseColor("#ffffff"))
        } else {
            holder.tvFastFolder.setTextColor(Color.parseColor("#ffffff"))
        }

        holder.itemView.setOnClickListener {
            if (listener != null) {
                listener.onFastFolderClick(item, position)
            }
        }
    }

    fun addItemFast(folder: FolderItem) {
        listFolder.add(folder)
        notifyItemInserted(listFolder.size - 1)
        notifyItemRangeChanged(0, listFolder.size - 1)
    }

    fun removeItem(pos: Int) {
        listFolder.removeAt(pos)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(0, listFolder.size)
    }

    fun setDataFastFolder(list: ArrayList<FolderItem>) {
        listFolder.clear()
        listFolder.addAll(list)
        notifyDataSetChanged()
    }
}