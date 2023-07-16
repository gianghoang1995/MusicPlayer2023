package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.AlbumItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.ArtworkUtils

class AdapterSearchAlbum(
    var context: Context?,
    var onClickItemAlbumListener: OnClickItemAlbumListener
) :
    RecyclerView.Adapter<AdapterSearchAlbum.SearchAlbumViewholder>() {
    private var listAlbum: ArrayList<AlbumItem> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchAlbumViewholder {
        var layoutInflater = LayoutInflater.from(context)
        return SearchAlbumViewholder(context, layoutInflater, parent)
    }

    override fun onBindViewHolder(holder: SearchAlbumViewholder, position: Int) {
        val item = listAlbum[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onClickItemAlbumListener.onClickItemAlbum(item)
        }
    }

    override fun getItemCount(): Int {
        return listAlbum.count()
    }

    fun setData(list: ArrayList<AlbumItem>) {
        listAlbum.clear()
        listAlbum.addAll(list)
        notifyDataSetChanged()
    }

    class SearchAlbumViewholder(
        private val context: Context?,
        inflater: LayoutInflater,
        parent: ViewGroup
    ) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.item_album_search, parent, false)) {
        var imgThumb: ShapeableImageView = itemView.findViewById(R.id.imgThumb)
        var tvAlbumName: TextView = itemView.findViewById(R.id.tvAlbumName)
        var tvCountSong: TextView = itemView.findViewById(R.id.tvCountSong)

        @SuppressLint("SetTextI18n")
        fun bind(item: AlbumItem) {
            Glide.with(itemView.context).load(ArtworkUtils.uri(item.id))
                .placeholder(R.drawable.ic_albums_transparent)
                .into(imgThumb)
            tvAlbumName.text = item.albumName.toString()
            tvCountSong.text =
                item.trackCount.toString() + " - " + context?.getString(R.string.title_songs)
        }
    }

    interface OnClickItemAlbumListener {
        fun onClickItemAlbum(item: AlbumItem)
    }
}