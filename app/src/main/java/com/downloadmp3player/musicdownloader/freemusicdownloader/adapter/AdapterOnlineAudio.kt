package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils.convertDuration
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline
import com.bumptech.glide.Glide
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import java.util.ArrayList

class AdapterOnlineAudio(
    var context: Context,
    var onItemSelected: OnClickItemOnlineListener?,
    var onLoadMoreListener: OnLoadMoreListener?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_ITEM = 1
    private val VIEW_PROG = 0
    private val lstAudio: ArrayList<ItemMusicOnline?> = ArrayList()
    private var canLoadMore = true

    interface OnLoadMoreListener {
        fun onLoadMore()
    }

    override fun getItemViewType(position: Int): Int {
        return if (lstAudio[position] != null) VIEW_ITEM else VIEW_PROG
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_ITEM) {
            ItemHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_online, parent, false)
            )
        } else {
            ProgressViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemHolder) {
            holder.bindData(position)
        }
    }

    override fun getItemCount() = lstAudio.size

    internal class ProgressViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var pBar: LottieAnimationView

        init {
            pBar = v.findViewById<View>(R.id.loadingView) as LottieAnimationView
        }
    }

    inner class ItemHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tv_title: TextView
        var img_thumb: ImageView
        var tv_duration: TextView
        fun bindData(position: Int) {
            val item = lstAudio[position]
            tv_title.text = item?.title
            tv_duration.text = convertDuration(item?.duration ?: 0)
            Glide.with(context)
                .load(item?.resourceThumb)
                .into(img_thumb)
            itemView.setOnClickListener { v: View? ->
                onItemSelected?.onClickItemOnline(item, position)
            }
        }

        init {
            tv_title = itemView.findViewById<View>(R.id.tv_name) as TextView
            img_thumb = itemView.findViewById<View>(R.id.img_thumb) as ImageView
            tv_duration = itemView.findViewById<View>(R.id.tv_duration) as TextView
        }
    }

    fun setData(lst: ArrayList<ItemMusicOnline?>) {
        lstAudio.clear()
        lstAudio.addAll(lst)
        notifyDataSetChanged()
    }

    fun showLoading() {
        if (canLoadMore) {
            canLoadMore = false
            lstAudio.add(null)
            notifyItemInserted(lstAudio.size - 1)
            onLoadMoreListener?.onLoadMore()
        }
    }

    fun addItemMore(lst: ArrayList<ItemMusicOnline?>) {
        val lastIndex = lstAudio.size - 1
        dismissLoading()
        lstAudio.addAll(lst)
        notifyItemRangeChanged(lastIndex, lstAudio.size)
    }

    fun dismissLoading() {
        canLoadMore = true
        if (lstAudio.size > 0) {
            val lastIndex = lstAudio.size - 1
            lstAudio.removeAt(lastIndex)
            notifyItemRemoved(lastIndex)
        }
    }

    interface OnClickItemOnlineListener {
        fun onClickItemOnline(item: ItemMusicOnline?, position: Int)
    }

    val isEmpty: Boolean
        get() = lstAudio.isEmpty()
}