package com.musicplayer.mp3player.playermusic.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.playermusic.adapter.viewholder.ThemesViewHolder
import com.musicplayer.mp3player.playermusic.utils.AppConstants

class ThemesAdapterRV(
    private val context: Context?,
    private val listener: OnItemThemesClickListener?
) :
    RecyclerView.Adapter<ThemesViewHolder>() {
    var listTheme = AppConstants.getListThemes(context)
    var index = 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemesViewHolder {
        var layoutInflater = LayoutInflater.from(context)
        return ThemesViewHolder(context, layoutInflater, parent)
    }

    override fun getItemCount(): Int {
        return listTheme.size
    }

    override fun onBindViewHolder(holder: ThemesViewHolder, position: Int) {
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