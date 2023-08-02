package com.musicplayer.mp3player.defaultmusicplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SectionIndexer
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.defaultmusicplayer.adapter.listener.OnClickArtistListener
import com.musicplayer.mp3player.defaultmusicplayer.adapter.viewholder.ArtistVHolder
import com.musicplayer.mp3player.defaultmusicplayer.model.ArtistItem
import java.util.*
import kotlin.collections.ArrayList

class ArtistAdapter(private val context: Context?, private val listener: OnClickArtistListener?) :
    RecyclerView.Adapter<ArtistVHolder>(),
    SectionIndexer {
    private var mSectionPositions: ArrayList<Int> = ArrayList()
    var listArtist = ArrayList<ArtistItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistVHolder {
        var layoutInflater = LayoutInflater.from(context)
        return ArtistVHolder(context, layoutInflater, parent)
    }

    override fun getItemCount(): Int {
        return listArtist.size
    }

    override fun onBindViewHolder(holder: ArtistVHolder, position: Int) {
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