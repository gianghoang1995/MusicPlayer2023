package com.musicplayer.mp3player.playermusic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SectionIndexer
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.playermusic.callback.ArtistListenerCallBack
import com.musicplayer.mp3player.playermusic.adapter.viewholder.ArtistViewHolder
import com.musicplayer.mp3player.playermusic.model.ArtistItem
import java.util.*
import kotlin.collections.ArrayList

class ArtistAdapterRV(private val context: Context?, private val listener: ArtistListenerCallBack?) :
    RecyclerView.Adapter<ArtistViewHolder>(),
    SectionIndexer {
    private var mSectionPositions: ArrayList<Int> = ArrayList()
    var listArtist = ArrayList<ArtistItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        var layoutInflater = LayoutInflater.from(context)
        return ArtistViewHolder(context, layoutInflater, parent)
    }

    override fun getItemCount(): Int {
        return listArtist.size
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(listArtist[position])

        holder.itemView.setOnClickListener {
            listener?.onArtistClick(listArtist[position], position)
        }

        holder.btnMore.setOnClickListener {
            listener?.onArtistMoreClick(listArtist[position], position, holder.btnMore)
        }
    }

    fun setDataArtist(listPin: ArrayList<ArtistItem>, listQuery: ArrayList<ArtistItem>) {
        listArtist.clear()
        listArtist.addAll(listPin)
        listArtist.addAll(listQuery)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        listArtist.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listArtist.size)
    }

    override fun getSections(): Array<String> {
        val sections: MutableList<String> = ArrayList()
        mSectionPositions = ArrayList()
        var i = 0
        val size: Int = listArtist.size
        while (i < size) {
            val section: String =
                java.lang.String.valueOf(listArtist[i].name?.get(0)).uppercase(Locale.getDefault())
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