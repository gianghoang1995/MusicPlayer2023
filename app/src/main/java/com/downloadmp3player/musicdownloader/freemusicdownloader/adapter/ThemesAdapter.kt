package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.viewholder.ThemesRvVHolder
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants

class ThemesAdapter(
    private val context: Context?,
    private val listener: OnItemThemesClickListener?
) :
    RecyclerView.Adapter<ThemesRvVHolder>() {
    var listTheme = AppConstants.getListThemes(context)
    var index = 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemesRvVHolder {
        var layoutInflater = LayoutInflater.from(context)
        return ThemesRvVHolder(context, layoutInflater, parent)
    }

    override fun getItemCount(): Int {
        return listTheme.size
    }

    override fun onBindViewHolder(holder: ThemesRvVHolder, position: Int) {
        holder.bind(listTheme[position], position)
        holder.itemView.setOnClickListener {
            listener?.onThemesSelected(position)
        }
        if (index == position) {
            holder.viewSelectedTheme.visibility = View.VISIBLE
        } else {
            holder.viewSelectedTheme.visibility = View.INVISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setIndexThemes(mIndex: Int) {
        index = mIndex
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCustomThemesPreview(bitmap: Bitmap?) {
        listTheme.removeAt(1)
        listTheme.add(1, bitmap)
        notifyDataSetChanged()
    }

    interface OnItemThemesClickListener {
        fun onThemesSelected(posThemes: Int)
    }
}