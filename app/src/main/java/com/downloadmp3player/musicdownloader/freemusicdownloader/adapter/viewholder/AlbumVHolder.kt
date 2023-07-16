package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.pin.AppPinDaoDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.database.pin.AppPinHelperDB
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.AlbumItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.ArtworkUtils

class AlbumVHolder(private val context: Context?, inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_album, parent, false)) {
    var pinAlbumHelper =
        AppPinHelperDB(context)
    var pinAlbumDao =
        AppPinDaoDB(pinAlbumHelper)
    var imgThumb: ImageView = itemView.findViewById(R.id.imgThumb)
    var tvTitle: TextView = itemView.findViewById(R.id.tv_album_name)
    var tvCount: TextView = itemView.findViewById(R.id.tv_count_album)
    var btnMore: ImageView = itemView.findViewById(R.id.btn_more_album)
    var imgPin: ImageView = itemView.findViewById(R.id.imgPin)

    @SuppressLint("SetTextI18n")
    fun bind(album: AlbumItem) {
        if (pinAlbumDao.isPinAlbum(album)) {
            imgPin.visibility = View.VISIBLE
        } else {
            imgPin.visibility = View.GONE
        }

        context?.let {
            Glide.with(it).load(ArtworkUtils.uri(album.id))
                .placeholder(R.drawable.ic_albums_transparent).transition(
                    DrawableTransitionOptions.withCrossFade()
                ).into(imgThumb)
        }
        tvTitle.text = album.albumName
        tvCount.text = "${album.trackCount} ${context?.getString(R.string.title_songs)}"
        tvTitle.isSelected = true
    }
}