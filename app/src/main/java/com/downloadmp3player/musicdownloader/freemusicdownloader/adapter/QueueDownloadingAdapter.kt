package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter

import android.content.Context
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils.convertDuration
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants.randomThumb
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline
import android.view.ViewGroup
import android.view.LayoutInflater
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import android.widget.TextView
import android.text.TextUtils
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class QueueDownloadingAdapter(
    private val context: Context,
    private val onlineClickListener: OnItemQueueClick?
) : RecyclerView.Adapter<QueueDownloadingAdapter.ItemOnlineViewHolder>() {
    private var lstQueue = ArrayList<ItemMusicOnline?>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemOnlineViewHolder {
        return ItemOnlineViewHolder(
            LayoutInflater.from(
                context
            ).inflate(R.layout.item_quee_download, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemOnlineViewHolder, position: Int) {
        val videoFromSearch = lstQueue[position]
        videoFromSearch?.let { holder.bind(it) }
        holder.btnDelete.setOnClickListener { v: View? ->
            onlineClickListener?.onClickDelete(
                videoFromSearch, position
            )
        }
    }

    fun setData(lst: ArrayList<ItemMusicOnline?>) {
        lstQueue.clear()
        lstQueue.addAll(lst)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return lstQueue.size
    }

    inner class ItemOnlineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgThumb: ImageView = itemView.findViewById(R.id.imgThumb)
        var tvName: TextView = itemView.findViewById(R.id.tvTitle)
        var tv_duration: TextView = itemView.findViewById(R.id.tvDuration)
        var btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: ItemMusicOnline?) {
            tvName.text = item?.title
            tvName.isSelected = true
            tv_duration.isSelected = true
            val duration =
                if (!TextUtils.isEmpty(item?.duration.toString())) item?.duration?.let {
                    convertDuration(
                        it
                    )
                } else context.getString(
                    R.string.unknow
                )
            tv_duration.text = duration
            Glide.with(context)
                .load(randomThumb())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontTransform()
                .placeholder(R.drawable.ic_song)
                .into(imgThumb)
        }
    }

    interface OnItemQueueClick {
        fun onClickDelete(item: ItemMusicOnline?, pos: Int)
    }
}