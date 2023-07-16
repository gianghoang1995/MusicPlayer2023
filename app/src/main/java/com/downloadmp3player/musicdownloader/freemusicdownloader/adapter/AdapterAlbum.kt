package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SectionIndexer
import androidx.recyclerview.widget.RecyclerView
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.listener.OnClickAlbumListener
import com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.viewholder.AlbumVHolder
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.AlbumItem
import java.util.*
import kotlin.collections.ArrayList

class AdapterAlbum(private val context: Context?, private val listener: OnClickAlbumListener?) :
    RecyclerView.Adapter<AlbumVHolder>(),
    SectionIndexer {
    private var mSectionPositions: ArrayList<Int> = ArrayList()
    var listAlbum = ArrayList<AlbumItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumVHolder {
        var layoutInflater = LayoutInflater.from(context)
        return AlbumVHolder(context, layoutInflater, parent)
    }

    override fun getItemCount(): Int {
        return listAlbum.size
    }

    override fun onBindViewHolder(holder: AlbumVHolder, position: Int) {
        var item = listAlbum[position]
        holder.bind(item)

        holder.itemView.setOnClickListener {
            listener?.onAlbumClick(item, position)
        }

        holder.btnMore.setOnClickListener {
            listener?.onAlbumMoreClick(item, position, holder.btnMore)
        }

    }

    fun setDataAlbum(listPin: ArrayList<AlbumItem>, listQuery: ArrayList<AlbumItem>) {
        listAlbum.clear()
        listAlbum.addAll(listPin)
        listAlbum.addAll(listQuery)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        listAlbum.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listAlbum.size)
    }

    override fun getSections(): Array<String> {
        val sections: MutableList<String> = ArrayList()
        mSectionPositions = ArrayList()
        var i = 0
        val size: Int = listAlbum.size
        while (i < size) {
            val section: String =
                java.lang.String.valueOf(listAlbum[i].albumName?.get(0)).uppercase(Locale.getDefault())
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