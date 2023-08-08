package com.musicplayer.mp3player.playermusic.adapter.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.imageview.ShapeableImageView
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.model.MusicItem
import com.musicplayer.mp3player.playermusic.utils.AppUtils
import com.musicplayer.mp3player.playermusic.utils.ArtworkUtils

class RingtoneViewHolder(
    private val context: Context?,
    inflater: LayoutInflater,
    parent: ViewGroup
) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_ringtone, parent, false)) {
    var imgThumb: ShapeableImageView = itemView.findViewById(R.id.imgThumb)
    var tvTittle: TextView = itemView.findViewById(R.id.tvTittle)
    var tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
    var btnMore: ImageButton = itemView.findViewById(R.id.btnMore)
    var nowPlayingView: FrameLayout = itemView.findViewById(R.id.nowPlayingView)

    fun bind(song: MusicItem) {
        context?.let {
            Glide.with(it).load(ArtworkUtils.getArtworkFromSongID(song.id))
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_song_transparent).into(imgThumb)
        }
        tvTittle.text = song.title
        var duration = ""
        var dur = song.duration
        var artist = song.artist
        dur = if (dur != null) {
            AppUtils.convertDuration(dur.toLong())
        } else {
            context?.getString(R.string.unknow)
        }
        if (artist == null) {
            artist = context?.getString(R.string.unknow)
        }
        duration = "$dur - $artist"
        tvDuration.text = duration
    }
}