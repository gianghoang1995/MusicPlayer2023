package com.musicplayer.mp3player.playermusic.adapter.viewholder

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
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.database.pin.AppPinDaoDB
import com.musicplayer.mp3player.playermusic.database.pin.AppPinHelperDB
import com.musicplayer.mp3player.playermusic.model.ArtistItem
import com.musicplayer.mp3player.playermusic.utils.ArtworkUtils

class ArtistViewHolder(private val context: Context?, inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_album, parent, false)) {
    var imgThumb: ImageView = itemView.findViewById(R.id.imgThumb)
    var tvTitle: TextView = itemView.findViewById(R.id.tv_album_name)
    var tvCount: TextView = itemView.findViewById(R.id.tv_count_album)
    var btnMore: ImageView = itemView.findViewById(R.id.btn_more_album)
    var imgPin: ImageView = itemView.findViewById(R.id.imgPin)
    var appPinHelper: AppPinHelperDB =
        AppPinHelperDB(context)
    var appPinDao: AppPinDaoDB =
        AppPinDaoDB(appPinHelper)

    @SuppressLint("SetTextI18n")
    fun bind(artist: ArtistItem) {
        if (appPinDao.isPinArtist(artist)) {
            imgPin.visibility = View.VISIBLE
        } else {
            imgPin.visibility = View.GONE
        }
        context?.let {
            Glide.with(it).load(ArtworkUtils.uri((artist.id!!.toLong())))
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_artists_transparent).into(imgThumb)
        }
        tvTitle.text = artist.name
        tvCount.text = artist.trackCount.toString() + " " + context?.getString(R.string.title_songs)
        tvTitle.isSelected = true
    }
}